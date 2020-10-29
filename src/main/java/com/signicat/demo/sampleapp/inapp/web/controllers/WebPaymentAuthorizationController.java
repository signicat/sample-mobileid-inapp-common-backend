package com.signicat.demo.sampleapp.inapp.web.controllers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.RSAKey;
import com.signicat.demo.sampleapp.inapp.common.OIDCProperties;
import com.signicat.demo.sampleapp.inapp.common.SessionData;
import com.signicat.demo.sampleapp.inapp.common.StateCache;
import com.signicat.demo.sampleapp.inapp.common.beans.AuthenticationResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.BaseResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.consentsign.Jwks;
import com.signicat.demo.sampleapp.inapp.common.exception.ApplicationException;
import com.signicat.demo.sampleapp.inapp.common.utils.OIDCUtils;
import com.signicat.demo.sampleapp.inapp.common.utils.WebAppUtils;
import com.signicat.demo.sampleapp.inapp.common.wsclient.ScidWsClient;
import com.signicat.generated.scid.Devices;

import net.minidev.json.JSONObject;

@RestController("WebPaymentAuthorizationController")
@RequestMapping("/web/authorizepayment")
@EnableAutoConfiguration
public class WebPaymentAuthorizationController {

    private static final Logger LOG = LoggerFactory.getLogger(WebPaymentAuthorizationController.class);

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
    public String index(final HttpServletRequest request, final HttpServletResponse response) {
        final String extRef = sessionData.getExtRef();
        if (extRef == null) {
            sessionData.setExtRef(defaultExternalRef);
        }
        return sessionData.getExtRef();
    }

    @GetMapping("/getDevices")
    public List<String> getDevicesbuttonClicked(
            @RequestParam(value = "externalRef", required = true) final String extRef,
            final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("PATH: getDevices  ('Get devices' button clicked)");

        final Devices fetchedDevices = ControllersUtil.getAllDevices(scidWsClient,extRef);
        final List<String> deviceNames = ControllersUtil.getListOfDeviceNames(fetchedDevices);

        sessionData.setExtRef(extRef);
        sessionData.setFetchedDevices(fetchedDevices);

        LOG.info("FETCHED DEVICES:" + deviceNames.toString());
        return deviceNames;
    }

    @GetMapping("/start")
    public void startAuthentication(
            @RequestParam(value = "externalRef", required = true) final String extRef,
            @RequestParam(value = "deviceName", required = true) final String devName,
            @RequestParam(value = "preContextTitle", required = false) final String preContextTitle,
            final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("PATH: /start ('Authenticate' button clicked)");

        final String deviceId = ControllersUtil.getDeviceId(sessionData.getFetchedDevices(), devName);
        String preContextTitleB64decoded = "";
        if(preContextTitle != null && !preContextTitle.isEmpty()) {
            preContextTitleB64decoded = new String(Base64.getDecoder().decode(preContextTitle));
        }

        final String state = OIDCUtils.createState(OIDCUtils.WEB_CHANNEL + getScrValues());
        LOG.info("InitialState:" + state);
        final String reqData = prepareEncryptedAuthenticationData(extRef, deviceId, preContextTitleB64decoded, state);
        LOG.info("Encrypted requewst:" + reqData);
        final String authorizeUrl = OIDCUtils.getEncryptedAuthorizeUri(oidcProperties.getOidcBase(), reqData);
        LOG.info("AUTHORIZE URL:" + authorizeUrl);

        final AuthenticationResponse authResponse = startAuthorizeFlow(authorizeUrl);
        if (authResponse.getError() != null) {
            throw new ApplicationException(authResponse.getError().getCode() + "-" + authResponse.getError().getMessage());
        }

        request.getSession().setMaxInactiveInterval(Integer.parseInt(this.sessionTimeout));
        sessionData.setAuthResponse(authResponse);
        final String stateKey = stateCache.storeToStateCache(state);
        sessionData.setStateKey(stateKey);

        LOG.info("AUTENTICATION RESPONSE:" + authResponse.toString());
        LOG.info("STATE KEY:" + stateKey);
    }

    @GetMapping("/checkStatus")
    public String checkStatus(final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("PATH: /checkStatus");
        return WebAppUtils.checkStatus(sessionData.getHttpClient(), sessionData.getAuthResponse().getStatusUrl());
    }

    @GetMapping("/doComplete")
    public BaseResponse doComplete(final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("PATH: /doComplete");
        return WebAppUtils.doComplete(sessionData.getHttpClient(), sessionData.getAuthResponse().getCompleteUrl(),
                sessionData.getStateKey());
    }

    private AuthenticationResponse startAuthorizeFlow(final String fullAuthorizeFlowURL) {
        return WebAppUtils.httpGet(sessionData.getHttpClient(), fullAuthorizeFlowURL, AuthenticationResponse.class,
                ImmutableMap.of(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString(),
                HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()));
    }

    private String prepareEncryptedAuthenticationData(final String extRef, final String devId,
            final String preContextTitle, final String initialState) {
        try {
            final RSAKey encyptionKey = getSignicatJWK();

            final JWEAlgorithm alg = JWEAlgorithm.parse(encyptionKey.getAlgorithm().getName());

            // Signicat OIDC server requires "kid" in header of JWE token
            final JWEHeader header = new JWEHeader.Builder(alg, EncryptionMethod.A256CBC_HS512).keyID(encyptionKey.getKeyID()).build();

            // Create a JWEObject with header and JSON payload
            final JWEObject jweObject = new JWEObject(header, new Payload(getCreatePayload(extRef, devId, preContextTitle, initialState)));

            // Create an encrypter with the specified public RSA key
            final RSAEncrypter encrypter = new RSAEncrypter(encyptionKey.toPublicJWK());

            // Encrypt JWEObject
            jweObject.encrypt(encrypter);

            // Serialize
            return jweObject.serialize();
        } catch (ParseException | JOSEException e) {
            throw new ApplicationException("Can not prepare encrypted object: " + e.getMessage());
        }
    }

    private JSONObject getCreatePayload(final String extRef, final String devId, final String preContextTitle, final String initialState){

        final String acr_values = getScrValues();

        final List<String> login_hint = new ArrayList<>();
        login_hint.add("externalRef-"+extRef);
        login_hint.add("deviceId-"+devId);
        if (preContextTitle != null) {
            login_hint.add("preContextTitle-"+preContextTitle);
        }

        final JSONObject payload_json = new JSONObject();
        payload_json.put("login_hint", login_hint);
        payload_json.put("ui_locales", "en");
        payload_json.put("scope", "openid profile");
        payload_json.put("acr_values", acr_values);
        payload_json.put("response_type", "code");
        payload_json.put("redirect_uri", oidcProperties.getRedirectUri());
        payload_json.put("state", initialState);
        payload_json.put("client_id", oidcProperties.getClientId());

        return payload_json;
    }

    private RSAKey getSignicatJWK() throws ParseException {
        // get JWKs from signicat, from https://<ENV>/oidc/jwks.json
        final Jwks keys = WebAppUtils.httpGet(sessionData.getHttpClient(), oidcProperties.getOidcBase() + OIDCUtils.JWKS_PATH, Jwks.class,
                ImmutableMap.of(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString()));

        // select first key listed as encryption use
        for(final JSONObject k : keys.getKeys()){
            if(k.get("use") .equals("enc")){
                return RSAKey.parse(k);
            }
        }

        throw new ApplicationException("No valid JWK found...");
    }

    private String getScrValues() {
        return oidcProperties.getAcrValues() + oidcProperties.getAuthMethod();
    }

}
