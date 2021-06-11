package com.signicat.demo.sampleapp.inapp.backend;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.signicat.demo.sampleapp.inapp.common.beans.*;
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
import com.signicat.demo.sampleapp.inapp.common.exception.ApplicationException;
import com.signicat.demo.sampleapp.inapp.common.utils.OIDCUtils;
import com.signicat.demo.sampleapp.inapp.common.utils.WebAppUtils;
import com.signicat.demo.sampleapp.inapp.common.wsclient.ScidWsClient;
import com.signicat.demo.sampleapp.inapp.common.wsclient.beans.ScidRequest;
import com.signicat.demo.sampleapp.inapp.common.utils.ControllersUtil;
import com.signicat.generated.scid.CreateArtifactResponse;

// ==========================================
// Web initiated - using OIDC interface
// ==========================================
@RestController("BackendRegisterController")
@RequestMapping("/backend/register")
@EnableAutoConfiguration
public class BackendRegisterController {

    private static final Logger LOG = LoggerFactory.getLogger(BackendRegisterController.class);

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

    private final HashMap<String, String[]> userDataMap = new HashMap<>();

    @GetMapping("/init")
    public InitResponse init() {
        LOG.info("PATH: /init");
        final String extRef = sessionData.getExtRef();
        final String devName = sessionData.getDevName();
        if (extRef == null) {
            sessionData.setExtRef(defaultExternalRef);
        }
        if (devName == null) {
            sessionData.setDevName(defaultDeviceName);
        }
        sessionData.setActivationCode(null);
        return new InitResponse(sessionData.getExtRef(), sessionData.getDevName());
    }

    @GetMapping("/info")
    public InitResponse info(
        @RequestParam(value = "activationCode") final String activationCode) {
        LOG.info("PATH: /info");
        // Since this endpoint is called by the mobile app at the end of the registration flow, there is a new session
        // object lacking the externalRef and deviceName. We use the activation code as the shared secret for the lookup.

        // NOTE: In a real world scenario, the mobile app would probably do a lockup against a database
        // passing the MobileID registrationId instead to get hold of externalRef and device name. In this demo, we
        // don't have a database.

        final String[] userData = this.userDataMap.get(activationCode);
        if (userData != null) {
            this.userDataMap.remove(activationCode);
            return new InitResponse(userData[0], userData[1]);
        }
        return new InitResponse(null, null);
    }

    @GetMapping("/start")
    public BaseResponse startRegistration(
            @RequestParam(value = "externalRef") final String extRef,
            @RequestParam(value = "deviceName") final String devName,
            final HttpServletRequest request) {
        LOG.info("PATH: /start");

        if (ControllersUtil.activationCodeIsNotErrorMessageAndDeviceAlreadyActivated(
                sessionData.getActivationCode(), scidWsClient, extRef, devName)) {
            sessionData.setActivationCode(ControllersUtil.STR_DEVICE_IS_ALREADY_ACTIVATED);
            return new ErrorResponse(ControllersUtil.STR_DEVICE_IS_ALREADY_ACTIVATED);
        }

        final RegistrationData serverData = prepareRegistrationData(extRef, devName);
        final String authorizeUrl = OIDCUtils.getAuthorizeUri(serverData);
        LOG.info("AUTHORIZE URL: {}", authorizeUrl);

        final RegistrationResponse regResponse = startAuthorizeFlow(authorizeUrl);
        if (regResponse.getError() != null) {
            throw new ApplicationException(regResponse.getError().getCode() + "-" + regResponse.getError().getMessage());
        }

        request.getSession().setMaxInactiveInterval(Integer.parseInt(this.sessionTimeout));
        sessionData.setExtRef(extRef);
        sessionData.setRegResponse(regResponse);

        // See note in "info" endpoint on why this is needed
        final String[] userData = {extRef, devName};
        this.userDataMap.put(regResponse.getActivationCode(), userData);

        final String stateKey = stateCache.storeToStateCache(serverData.getState());
        sessionData.setStateKey(stateKey);

        LOG.info("REGISTRATION RESPONSE: {}", regResponse);
        LOG.info("STATE KEY: {}", stateKey);
        return new SuccessResponse(regResponse.getActivationCode());
    }

    @GetMapping("/checkStatus")
    public String checkStatus() {
        LOG.info("PATH: /checkStatus");
        return WebAppUtils.checkStatus(sessionData.getHttpClient(), sessionData.getRegResponse().getStatusUrl());
    }

    @GetMapping("/doComplete")
    public BaseResponse doComplete() {
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
        LOG.info("Generate Artifact...");
        final String artifact = createArtifact(extRef);

        LOG.info("Artifact created... {}", artifact);
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
