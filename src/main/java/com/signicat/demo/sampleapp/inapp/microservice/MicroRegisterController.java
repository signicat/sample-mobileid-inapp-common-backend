package com.signicat.demo.sampleapp.inapp.microservice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signicat.demo.sampleapp.inapp.common.OIDCProperties;
import com.signicat.demo.sampleapp.inapp.common.SessionData;
import com.signicat.demo.sampleapp.inapp.common.beans.InitResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.RegistrationResponse;
import com.signicat.demo.sampleapp.inapp.common.exception.ApplicationException;
import com.signicat.demo.sampleapp.inapp.common.utils.WebAppUtils;
import com.signicat.demo.sampleapp.inapp.common.wsclient.ScidWsClient;
import com.signicat.demo.sampleapp.inapp.microservice.beans.BaseMicroserviceResponse;
import com.signicat.demo.sampleapp.inapp.microservice.utils.AccessTokenFetcher;
import com.signicat.demo.sampleapp.inapp.common.utils.ControllersUtil;

// ==========================================
// Web initiated - using REST interface
// ==========================================
@RestController("MicroRegisterController")
@RequestMapping("/microservice/register")
@EnableAutoConfiguration
public class MicroRegisterController {

    private static final Logger LOG = LoggerFactory.getLogger(MicroRegisterController.class);

    @Autowired
    private OIDCProperties oidcProperties;

    @Autowired
    protected ScidWsClient scidWsClient;

    @Value("${sample.externalRef}")
    private String              defaultExternalRef;

    @Value("${sample.deviceName}")
    private String              defaultDeviceName;

    @Value("${server.servlet.session.timeout}")
    private String              sessionTimeout;

    @Autowired
    private SessionData sessionData;

    private final HashMap<String, String[]> userDataMap = new HashMap<>();

    @Value("${microservice.contextPath}")
    private String  serviceContextPath;

    @Value("${microservice.baseUrl}")
    private String  baseUrl;

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
        sessionData.setActivationCode(null);

        // set access token fetcher
        sessionData.setAccessTokenFetcher(new AccessTokenFetcher(oidcProperties));

        return new InitResponse(sessionData.getExtRef(), sessionData.getDevName());
    }

    @GetMapping("/info")
    public InitResponse info(
        @RequestParam(value = "activationCode", required = true) final String activationCode) {
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
    public String startRegistration(
            @RequestParam(value = "externalRef", required = true) final String extRef,
            @RequestParam(value = "deviceName", required = true) final String devName,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        LOG.info("PATH: /start ('Register' button clicked)");

        if (ControllersUtil.activationCodeIsNotErrorMessageAndDeviceAlreadyActivated(
                sessionData.getActivationCode(), scidWsClient, extRef, devName)) {
            sessionData.setActivationCode(ControllersUtil.STR_DEVICE_IS_ALREADY_ACTIVATED);
            return ControllersUtil.STR_DEVICE_IS_ALREADY_ACTIVATED;
        }

        sessionData.setExtRef(extRef);
        sessionData.setDevName(devName);

        final RegistrationResponse regResponse = startRgistrationFlow();
        if (regResponse.getError() != null) {
            throw new ApplicationException(regResponse.getError().getCode() + "-" + regResponse.getError().getMessage());
        }

        request.getSession().setMaxInactiveInterval(Integer.parseInt(this.sessionTimeout));
        sessionData.setExtRef(extRef);
        sessionData.setRegResponse(regResponse);

        // See note in "info" endpoint on why this is needed
        final String[] userData = {extRef, devName};
        this.userDataMap.put(regResponse.getActivationCode(), userData);
        LOG.info("REGISTRATION RESPONSE:" + regResponse.toString());
        return regResponse.getActivationCode();
    }

    private RegistrationResponse startRgistrationFlow() throws Exception {
        final String registrationStartUrl = baseUrl+serviceContextPath+"register/start";
        final HttpPost httpPost = new HttpPost(registrationStartUrl);
        httpPost.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + sessionData.getAccessTokenFetcher().getValidAccessToken());
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());

        String message;
        final JSONObject json = new JSONObject();
        json.put("externalRef", sessionData.getExtRef());
        json.put("deviceName", sessionData.getDevName());
        message = json.toString();
        final StringEntity entity = new StringEntity(message);
        httpPost.setEntity(entity);

        RegistrationResponse registrationResponse = null;
        final ObjectMapper mapper = new ObjectMapper();
        try (CloseableHttpResponse httpResponse = sessionData.getHttpClient().execute(httpPost)) {
            final String content = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            registrationResponse = mapper.readValue(content, RegistrationResponse.class);
            return registrationResponse;
        } catch (final IOException e) {
            throw new ApplicationException("Start registration failed: " + e.getMessage());
        }
    }

    @GetMapping("/checkStatus")
    public String checkStatus(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        LOG.info("PATH: /checkStatus");
        return WebAppUtils.checkStatusMicroservice(sessionData.getHttpClient(),
                sessionData.getRegResponse().getStatusUrl(),
                sessionData.getAccessTokenFetcher().getValidAccessToken());
    }

    @GetMapping("/doComplete")
    public BaseMicroserviceResponse doComplete(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        LOG.info("PATH: /doComplete");
        final BaseMicroserviceResponse result =  WebAppUtils.doCompleteMicroservice(sessionData.getHttpClient(),
                sessionData.getRegResponse().getCompleteUrl(),
                sessionData.getAccessTokenFetcher().getValidAccessToken());
        return result;
    }
}
