package com.signicat.demo.sampleapp.inapp.backend;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import com.signicat.demo.sampleapp.inapp.common.beans.AuthenticationData;
import com.signicat.demo.sampleapp.inapp.common.beans.AuthenticationResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.BaseResponse;
import com.signicat.demo.sampleapp.inapp.common.exception.ApplicationException;
import com.signicat.demo.sampleapp.inapp.common.utils.OIDCUtils;
import com.signicat.demo.sampleapp.inapp.common.utils.WebAppUtils;
import com.signicat.demo.sampleapp.inapp.common.wsclient.ScidWsClient;
import com.signicat.demo.sampleapp.inapp.common.utils.ControllersUtil;
import com.signicat.generated.scid.Devices;

// ==========================================
// Web initiated - using OIDC interface
// ==========================================
@RestController("BackendAuthenticateController")
@RequestMapping("/backend/authenticate")
@EnableAutoConfiguration
public class BackendAuthenticateController {

    private static final Logger LOG = LoggerFactory.getLogger(BackendAuthenticateController.class);

    @Autowired
    private OIDCProperties      oidcProperties;

    @Autowired
    private ScidWsClient        scidWsClient;

    @Value("${sample.externalRef}")
    private String              defaultExternalRef;

    @Value("${server.servlet.session.timeout}")
    private String              sessionTimeout;

    @Autowired
    private SessionData         sessionData;

    @Autowired
    private StateCache          stateCache;

    @GetMapping("/init")
    public String init() {
        LOG.info("PATH: /init");
        final String extRef = sessionData.getExtRef();
        if (extRef == null) {
            sessionData.setExtRef(defaultExternalRef);
        }
        return sessionData.getExtRef();
    }

    @GetMapping("/getDevices")
    public List<String> getDevicesbuttonClicked(
            @RequestParam(value = "externalRef") final String extRef) {
        LOG.info("PATH: /getDevices");

        final Devices fetchedDevices = ControllersUtil.getAllDevices(scidWsClient,extRef);
        final List<String> deviceNames = ControllersUtil.getListOfDeviceNames(fetchedDevices);

        sessionData.setExtRef(extRef);
        sessionData.setFetchedDevices(fetchedDevices);

        LOG.info("FETCHED DEVICES: {}", deviceNames);
        return deviceNames;
    }

    @GetMapping("/start")
    public void startAuthentication(
            @RequestParam(value = "externalRef") final String extRef,
            @RequestParam(value = "deviceName") final String devName,
            @RequestParam(value = "pushPayload", required = false) final String pushPayload,
            final HttpServletRequest request) {
        LOG.info("PATH: /start");

        final String deviceId = ControllersUtil.getDeviceId(sessionData.getFetchedDevices(), devName);

        final String state = OIDCUtils.createState(OIDCUtils.WEB_CHANNEL + getScrValues());
        final AuthenticationData serverData = prepareAuthenticationData(extRef, deviceId, pushPayload, state);
        final String authorizeUrl = OIDCUtils.getAuthorizeUri(serverData);
        LOG.info("AUTHORIZE URL: {}", authorizeUrl);

        final AuthenticationResponse authResponse = startAuthorizeFlow(authorizeUrl);
        if (authResponse.getError() != null) {
            throw new ApplicationException(authResponse.getError().getCode() + "-" + authResponse.getError().getMessage());
        }

        request.getSession().setMaxInactiveInterval(Integer.parseInt(this.sessionTimeout));
        sessionData.setAuthResponse(authResponse);
        final String stateKey = stateCache.storeToStateCache(state);
        sessionData.setStateKey(stateKey);

        LOG.info("AUTHENTICATION RESPONSE: {}", authResponse);
        LOG.info("STATE KEY: {}", stateKey);
    }

    @GetMapping("/checkStatus")
    public String checkStatus() {
        LOG.info("PATH: /checkStatus");
        return WebAppUtils.checkStatus(sessionData.getHttpClient(), sessionData.getAuthResponse().getStatusUrl());
    }

    @GetMapping("/doComplete")
    public BaseResponse doComplete() {
        LOG.info("PATH: /doComplete");
        return WebAppUtils.doComplete(sessionData.getHttpClient(), sessionData.getAuthResponse().getCompleteUrl(),
                sessionData.getStateKey());
    }

    private AuthenticationResponse startAuthorizeFlow(final String fullAuthorizeFlowURL) {
        return WebAppUtils.httpGet(sessionData.getHttpClient(), fullAuthorizeFlowURL, AuthenticationResponse.class,
                ImmutableMap.of(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString(),
                HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()));
    }

    private AuthenticationData prepareAuthenticationData(final String extRef, final String devId, final String pushPayload, final String state) {
        return new AuthenticationData(oidcProperties.getOidcBase() + WebAppUtils.AUTHORIZE_RESPONSE_TYPE_CODE, oidcProperties.getClientId(),
                oidcProperties.getScope(),
                oidcProperties.getRedirectUri(),
                oidcProperties.getAcrValues() + oidcProperties.getAuthMethod(), state, extRef, devId, "", pushPayload);
    }

    private String getScrValues() {
        return oidcProperties.getAcrValues() + oidcProperties.getAuthMethod();
    }

}
