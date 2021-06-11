package com.signicat.demo.sampleapp.inapp.backend;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
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
import com.signicat.demo.sampleapp.inapp.common.utils.ControllersUtil;
import com.signicat.generated.scid.Devices;

import net.minidev.json.JSONObject;

// ==========================================
// Web initiated - using OIDC interface
// ==========================================
@RestController("BackendPaymentAuthorizationController")
@RequestMapping("/backend/authorizepayment")
@EnableAutoConfiguration
public class BackendPaymentAuthorizationController {

    private static final Logger LOG = LoggerFactory.getLogger(BackendPaymentAuthorizationController.class);

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
            @RequestParam(value = "preContextTitle", required = false) final String preContextTitle,
            @RequestParam(value = "pushPayload", required = false) final String pushPayload,
            final HttpServletRequest request) {
        LOG.info("PATH: /start");

        final String deviceId = ControllersUtil.getDeviceId(sessionData.getFetchedDevices(), devName);
        String preContextTitleB64decoded = "";
        if(preContextTitle != null && !preContextTitle.isEmpty()) {
            preContextTitleB64decoded = new String(Base64.getDecoder().decode(preContextTitle));
        }

        final String state = OIDCUtils.createState(OIDCUtils.WEB_CHANNEL + getScrValues());
        LOG.info("InitialState: {}", state);
        final String reqData = prepareEncryptedAuthenticationData(extRef, deviceId, preContextTitleB64decoded, pushPayload, state);
        LOG.info("Encrypted request: {}", reqData);
        final String authorizeUrl = OIDCUtils.getEncryptedAuthorizeUri(oidcProperties.getOidcBase(), reqData);
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

    private String prepareEncryptedAuthenticationData(final String extRef, final String devId,
            final String preContextTitle, final String pushPayload, final String initialState) {
        try {
            final RSAKey encyptionKey = getSignicatJWK();

            final JWEAlgorithm alg = JWEAlgorithm.parse(encyptionKey.getAlgorithm().getName());

            // Signicat OIDC server requires "kid" in header of JWE token
            final JWEHeader header = new JWEHeader.Builder(alg, EncryptionMethod.A256CBC_HS512).keyID(encyptionKey.getKeyID()).build();

            // Create a JWEObject with header and JSON payload
            final JWEObject jweObject = new JWEObject(header, new Payload(getCreatePayload(extRef, devId, preContextTitle, pushPayload, initialState)));

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

    private JSONObject getCreatePayload(final String extRef, final String devId, final String preContextTitle, final String pushPayload, final String initialState){

        final String acr_values = getScrValues();

        final List<String> loginHint = new ArrayList<>();
        loginHint.add("externalRef-"+extRef);
        loginHint.add("deviceId-"+devId);
        if (preContextTitle != null) {
            loginHint.add("preContextTitle-"+preContextTitle);
        }
        if (!Strings.isNullOrEmpty(pushPayload)) {
            loginHint.add("pushPayload-" + pushPayload);
        }

        final JSONObject payloadJson = new JSONObject();
        payloadJson.put("login_hint", loginHint);
        payloadJson.put("ui_locales", "en");
        payloadJson.put("scope", "openid mobileid profile");
        payloadJson.put("acr_values", acr_values);
        payloadJson.put("response_type", "code");
        payloadJson.put("redirect_uri", oidcProperties.getRedirectUri());
        payloadJson.put("state", initialState);
        payloadJson.put("client_id", oidcProperties.getClientId());

        return payloadJson;
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
