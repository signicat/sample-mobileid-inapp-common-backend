package com.signicat.demo.sampleapp.inapp.microservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.signicat.demo.sampleapp.inapp.common.OIDCProperties;
import com.signicat.demo.sampleapp.inapp.common.SessionData;
import com.signicat.demo.sampleapp.inapp.common.StateCache;
import com.signicat.demo.sampleapp.inapp.common.beans.SignResponse;
import com.signicat.demo.sampleapp.inapp.common.exception.ApplicationException;
import com.signicat.demo.sampleapp.inapp.common.utils.WebAppUtils;
import com.signicat.demo.sampleapp.inapp.common.wsclient.ScidWsClient;
import com.signicat.demo.sampleapp.inapp.microservice.utils.AccessTokenFetcher;
import com.signicat.demo.sampleapp.inapp.microservice.beans.CompleteSignMicroserviceResponse;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController("MicroConsentSignController")
@RequestMapping("/microservice/consentsign")
@EnableAutoConfiguration
public class MicroConsentSignController {

    private static final Logger LOG = LoggerFactory.getLogger(MicroConsentSignController.class);

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

    @Autowired
    private StateCache stateCache;

    @Value("${microservice.contextPath}")
    private String  serviceContextPath;

    @Value("${microservice.baseUrl}")
    private String  baseUrl;

    @GetMapping("/init")
    public String index(final HttpServletRequest request, final HttpServletResponse response) {
        final String extRef = sessionData.getExtRef();
        if (extRef == null) {
            sessionData.setExtRef(defaultExternalRef);
        }

        // set access token fetcher
        sessionData.setAccessTokenFetcher(new AccessTokenFetcher(oidcProperties));

        return sessionData.getExtRef();
    }

    @GetMapping("/getDevices")
    public List<String> getDevicesbuttonClicked(
            @RequestParam(value = "externalRef", required = true) final String extRef,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        LOG.info("PATH: getDevices  ('Get devices' button clicked)");

        String devicesUrl = baseUrl+serviceContextPath+"authentication/devices/" + extRef;
        final List<String> deviceNames =
                WebAppUtils.httpGet(sessionData.getHttpClient(), devicesUrl, ArrayList.class,
                        ImmutableMap.of(
                                HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString(),
                                HttpHeaders.AUTHORIZATION, "Bearer " + sessionData.getAccessTokenFetcher().getValidAccessToken()
                        ));

        LOG.info("FETCHED DEVICES:" + deviceNames.toString());
        return deviceNames;
    }

    // TODO consider sending extRef as param here as well. Otherwise it will be null if it is not initialized on init
    @GetMapping("/start")
    public void startConsentSign(
            @RequestParam(value = "deviceName", required = true) final String devName,
            @RequestParam(value = "preContextTitle", required = false) final String preContextTitle,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        LOG.info("PATH: /start ('Authenticate' button clicked)");

        sessionData.setDevName(devName);

        String preContextTitleB64decoded = "";
        if(preContextTitle != null && !preContextTitle.isEmpty()) {
            preContextTitleB64decoded = new String(Base64.getDecoder().decode(preContextTitle));
        }

        final SignResponse signResponse = startConsentSignFlow(preContextTitleB64decoded);
        if (signResponse.getError() != null) {
            throw new ApplicationException(signResponse.getError().getCode() + "-" + signResponse.getError().getMessage());
        }

        request.getSession().setMaxInactiveInterval(Integer.parseInt(this.sessionTimeout));
        sessionData.setSignResponse(signResponse);

        LOG.info("CONSENT SIGN RESPONSE:" + signResponse.toString());
    }

    @GetMapping("/checkStatus")
    public String checkStatus(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        LOG.info("PATH: /checkStatus");
        return WebAppUtils.checkStatusMicroservice(sessionData.getHttpClient(),
                sessionData.getSignResponse().getStatusUrl(),
                sessionData.getAccessTokenFetcher().getValidAccessToken());
    }

    @GetMapping("/doComplete")
    public CompleteSignMicroserviceResponse doComplete(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        LOG.info("PATH: /doComplete");
        CompleteSignMicroserviceResponse result =  WebAppUtils.doCompleteSignMicroservice(sessionData.getHttpClient(),
                sessionData.getSignResponse().getCompleteUrl(),
                sessionData.getAccessTokenFetcher().getValidAccessToken());
        return result;
    }

    private SignResponse startConsentSignFlow(final String consentText) throws Exception {
        final String signStartUrl = baseUrl+serviceContextPath+"sign/start";
        final HttpPost httpPost = new HttpPost(signStartUrl);
        httpPost.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + sessionData.getAccessTokenFetcher().getValidAccessToken());
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());

        String message;
        final org.json.simple.JSONObject json = new JSONObject();
        json.put("externalRef", sessionData.getExtRef());
        json.put("deviceName", sessionData.getDevName());
        json.put("consentText", consentText);
        message = json.toString();
        StringEntity entity = new StringEntity(message);
        httpPost.setEntity(entity);

        SignResponse signResponse = null;
        ObjectMapper mapper = new ObjectMapper();
        try (CloseableHttpResponse httpResponse = sessionData.getHttpClient().execute(httpPost)) {
            signResponse = mapper.readValue(EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8), SignResponse.class);
            return signResponse;
        } catch (final IOException e) {
            throw new ApplicationException("Start consent sign failed: " + e.getMessage());
        }
    }
}
