package com.signicat.demo.sampleapp.inapp.web.controllers;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.signicat.demo.sampleapp.inapp.common.OIDCProperties;
import com.signicat.demo.sampleapp.inapp.common.SessionData;
import com.signicat.demo.sampleapp.inapp.common.StateCache;
import com.signicat.demo.sampleapp.inapp.common.beans.RegistrationData;
import com.signicat.demo.sampleapp.inapp.common.beans.BaseResponse;
import com.signicat.demo.sampleapp.inapp.common.exception.ApplicationException;
import com.signicat.demo.sampleapp.inapp.common.utils.OIDCUtils;
import com.signicat.demo.sampleapp.inapp.common.wsclient.ScidWsClient;
import com.signicat.demo.sampleapp.inapp.common.wsclient.beans.ScidRequest;
import com.signicat.demo.sampleapp.inapp.common.beans.InitResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.RegistrationResponse;
import com.signicat.demo.sampleapp.inapp.common.utils.WebAppUtils;
import com.signicat.generated.scid.CreateArtifactResponse;

@RestController("WebRegisterController")
@RequestMapping("/web/register")
@EnableAutoConfiguration
public class WebRegisterController {

    private static final Logger LOG = LoggerFactory.getLogger(WebRegisterController.class);

    @Autowired
    private OIDCProperties      oidcProperties;

    @Autowired
    protected ScidWsClient      scidWsClient;

    @Value("${sample.externalRef}")
    private String              defaultExternalRef;

    @Value("${sample.deviceName}")
    private String              defaultDeviceName;

    @Value("${server.servlet.session.timeout}")
    private String              sessionTimeout;

    @Autowired
    private SessionData         sessionData;

    @Autowired
    private StateCache          stateCache;

    @GetMapping("/init")
    public InitResponse index(final HttpServletRequest request, final HttpServletResponse response) {
        final String extRef = sessionData.getExtRef();
        final String devName = sessionData.getDevName();
        if (extRef == null) {
            sessionData.setExtRef(defaultExternalRef);
        }
        if (devName == null) {
            sessionData.setDevName(defaultDeviceName);
        }
        return new InitResponse(sessionData.getExtRef(), sessionData.getDevName());
    }

    @GetMapping("/start")
    public String startRegistration(
            @RequestParam(value = "externalRef", required = true) final String extRef,
            @RequestParam(value = "deviceName", required = true) final String devName,
            final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("PATH: /start ('Register' button clicked)");

        final RegistrationData serverData = prepareRegistrationData(extRef, devName);
        final String authorizeUrl = OIDCUtils.getAuthorizeUri(serverData);
        LOG.info("AUTHORIZE URL:" + authorizeUrl);

        final RegistrationResponse regResponse = startAuthorizeFlow(authorizeUrl);
        if (regResponse.getError() != null) {
            throw new ApplicationException(regResponse.getError().getCode() + "-" + regResponse.getError().getMessage());
        }

        request.getSession().setMaxInactiveInterval(Integer.parseInt(this.sessionTimeout));
        sessionData.setExtRef(extRef);
        sessionData.setRegResponse(regResponse);

        final String stateKey = stateCache.storeToStateCache(serverData.getState());
        sessionData.setStateKey(stateKey);

        LOG.info("REGISTRATION RESPONSE:" + regResponse.toString());
        LOG.info("STATE KEY:" + stateKey);
        return regResponse.getActivationCode();
    }

    @GetMapping("/checkStatus")
    public String checkStatus(final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("PATH: /checkStatus");
        return WebAppUtils.checkStatus(sessionData.getHttpClient(), sessionData.getRegResponse().getStatusUrl());
    }

    @GetMapping("/doComplete")
    public BaseResponse doComplete(final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("PATH: /doComplete");
        return WebAppUtils.doComplete(sessionData.getHttpClient(), sessionData.getRegResponse().getCompleteUrl(),
                sessionData.getStateKey());
    }

    private RegistrationResponse startAuthorizeFlow(final String fullAuthorizeFlowURL) {
        return WebAppUtils.httpGet(sessionData.getHttpClient(), fullAuthorizeFlowURL, RegistrationResponse.class,
                ImmutableMap.of(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString()));
    }

    private RegistrationData prepareRegistrationData(final String extRef, final String devName) {
        // --- Generate Artifact
        final String artifact = createArtifact(extRef);

        final String state = OIDCUtils.createState(OIDCUtils.WEB_CHANNEL + oidcProperties.getAcrValues() + oidcProperties.getRegMethod());

        return new RegistrationData(oidcProperties.getOidcBase() + WebAppUtils.AUTHORIZE_RESPONSE_TYPE_CODE, oidcProperties.getClientId(),
                oidcProperties.getScope(),
                oidcProperties.getRedirectUri(),
                oidcProperties.getAcrValues() + oidcProperties.getRegMethod(), state, extRef, devName, artifact);
    }

    private String createArtifact(final String extRef) {
        // --- Account creation is skipped if account already exits
        if (!scidWsClient.accountExists(extRef)) {
            final ScidRequest scidRequest = new ScidRequest(extRef, new ArrayList<>());
            scidWsClient.createAccount(scidRequest);
        }

        // --- Create artifact
        final CreateArtifactResponse artifactResult = (CreateArtifactResponse) scidWsClient.createArtifact(extRef);
        return artifactResult.getArtifact();
    }
}
