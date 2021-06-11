package com.signicat.demo.sampleapp.inapp.backend;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
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

import com.google.common.base.Strings;
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
import com.signicat.demo.sampleapp.inapp.common.beans.BaseResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.InitSignResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.SignResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.SignStatusResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.consentsign.Jwks;
import com.signicat.demo.sampleapp.inapp.common.exception.ApplicationException;
import com.signicat.demo.sampleapp.inapp.common.utils.ControllersUtil;
import com.signicat.demo.sampleapp.inapp.common.utils.OIDCUtils;
import com.signicat.demo.sampleapp.inapp.common.utils.WebAppUtils;
import com.signicat.demo.sampleapp.inapp.common.wsclient.ScidWsClient;
import com.signicat.generated.scid.Devices;

import net.minidev.json.JSONObject;

// ==========================================
// Web initiated - using OIDC interface
// ==========================================
@RestController("BackendConsentSignController")
@RequestMapping("/backend/consentsign")
@EnableAutoConfiguration
public class BackendConsentSignController {

    private static final Logger LOG = LoggerFactory.getLogger(BackendConsentSignController.class);

    @Autowired
    private OIDCProperties oidcProperties;

    @Autowired
    private ScidWsClient scidWsClient;

    @Value("${sample.externalRef}")
    private String              defaultExternalRef;

    @Value("${server.servlet.session.timeout}")
    private String              sessionTimeout;

    @Autowired
    private SessionData sessionData;

    @Autowired
    private StateCache stateCache;

    @GetMapping("/init")
    public InitSignResponse init() {
        LOG.info("PATH: /init");
        final String extRef = sessionData.getExtRef();
        if (extRef == null) {
            sessionData.setExtRef(defaultExternalRef);
        }
        return new InitSignResponse(sessionData.getExtRef(), null, !Strings.isNullOrEmpty(oidcProperties.getConsentSignMethodJwt()));
    }

    @GetMapping("/getDevices")
    public List<String> getDevicesbuttonClicked(
            @RequestParam(value = "externalRef") final String extRef) {
        LOG.info("PATH: /getDevices");

        final Devices fetchedDevices = ControllersUtil.getAllDevices(scidWsClient, extRef);
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
            @RequestParam(value = "sdoFormat") final String sdoFormat,
            @RequestParam(value = "preContextTitle", required = false) final String preContextTitle,
            @RequestParam(value = "preContextContent", required = false) final String preContextContent,
            @RequestParam(value = "pushPayload", required = false) final String pushPayload,
            final HttpServletRequest request) {
        LOG.info("PATH: /start (with: {})", sdoFormat);

        final String deviceId = ControllersUtil.getDeviceId(sessionData.getFetchedDevices(), devName);
        String preContextTitleB64decoded = "";
        if(preContextTitle != null && !preContextTitle.isEmpty()) {
            preContextTitleB64decoded = new String(Base64.getDecoder().decode(preContextTitle));
        }
        if (preContextTitleB64decoded.isEmpty()) {
            throw new ApplicationException("Consent text cannot be empty");
        }

        try {
            final String initialState = OIDCUtils.createState(OIDCUtils.SIGN_CHANNEL + getScrValues("default"));
            LOG.info("Sign initialState: {}", initialState);

            final String reqData = prepareSignData(extRef, deviceId, sdoFormat, preContextTitleB64decoded, preContextContent, pushPayload, initialState);
            LOG.info("reqData: {}", reqData);

            // make signUrl
            final StringBuilder sb = new StringBuilder();
            sb.append(oidcProperties.getOidcBase());
            sb.append("authorize?request=").append(reqData);
            final String signUrl = sb.toString();
            LOG.info("Sign URL: {}", signUrl);

            // send sign request
            final SignResponse signResponse = startSignFlow(signUrl);
            LOG.info("Sign RESPONSE: {}", signResponse);
            if (signResponse.getError() != null) {
                throw new ApplicationException(signResponse.getError().getCode() + "-" + signResponse.getError().getMessage());
            }

            request.getSession().setMaxInactiveInterval(Integer.parseInt(this.sessionTimeout));
            sessionData.setSignResponse(signResponse);

            final String stateKey = stateCache.storeToStateCache(initialState);
            sessionData.setStateKey(stateKey);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ApplicationException(e.getMessage());
        }

    }

    @GetMapping("/checkStatus")
    public String checkStatus() {
        LOG.info("PATH: /checkStatus");
        final SignStatusResponse st = WebAppUtils.checkSignStatus(sessionData.getHttpClient(), sessionData.getSignResponse().getStatusUrl());
        return st.getStatus();
    }

    @GetMapping("/doComplete")
    public BaseResponse doComplete() {
        LOG.info("PATH: /doComplete");
        return WebAppUtils.doComplete(sessionData.getHttpClient(), sessionData.getSignResponse().getCompleteUrl(),
                sessionData.getStateKey());
    }

    private SignResponse startSignFlow(final String signFlowURL) {
        return WebAppUtils.httpGet(sessionData.getHttpClient(), signFlowURL, SignResponse.class,
                ImmutableMap.of(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString(),
                        HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()));
    }

    private String prepareSignData(final String extRef, final String devId, final String sdoFormat, final String preContextTitle, final String preContextContent, final String pushPayload, final String initialState) throws JOSEException, ParseException {
        final RSAKey encyptionKey = getSignicatJWK();

        final JWEAlgorithm alg = JWEAlgorithm.parse(encyptionKey.getAlgorithm().getName());

        // Signicat OIDC server requires "kid" in header of JWE token
        final JWEHeader header = new JWEHeader.Builder(alg, EncryptionMethod.A256CBC_HS512).keyID(encyptionKey.getKeyID()).build();

        // Create a JWEObject with header and JSON payload
        final JWEObject jweObject = new JWEObject(header, new Payload(getCreatePayload(extRef, devId, sdoFormat, preContextTitle, preContextContent, pushPayload, initialState)));

        // Create an encrypter with the specified public RSA key
        final RSAEncrypter encrypter = new RSAEncrypter(encyptionKey.toPublicJWK());

        // Encrypt JWEObject
        jweObject.encrypt(encrypter);

        // Serialize
        return jweObject.serialize();
    }

    private JSONObject getCreatePayload(final String extRef, final String devId,  final String sdoFormat, final String preContextTitle, final String preContextContent, final String pushPayload, final String initialState){

        final String acr_values = getScrValues(sdoFormat);

        final List<String> login_hint = new ArrayList<>();
        login_hint.add("externalRef-"+extRef);
        login_hint.add("deviceId-"+devId);
        if (preContextContent != null) {
            login_hint.add("metaData-"+preContextContent);
        }
        if (!Strings.isNullOrEmpty(pushPayload)) {
            login_hint.add("pushPayload-" + pushPayload);
        }

        final JSONObject payload_json = new JSONObject();
        payload_json.put("login_hint", login_hint);
        payload_json.put("ui_locales", "en");
        payload_json.put("scope", "openid profile signicat.sign");
        payload_json.put("signicat_signtext", preContextTitle);
        payload_json.put("acr_values", acr_values);
        payload_json.put("response_type", "code");
        payload_json.put("redirect_uri", oidcProperties.getRedirectUri());
        payload_json.put("state", initialState);
        payload_json.put("client_id", oidcProperties.getClientId());

        return payload_json;
    }

    private String getScrValues(final String sdoFormat) {
        final String method = (sdoFormat.equalsIgnoreCase("jwt") && oidcProperties.getConsentSignMethodJwt() != null)
                ? oidcProperties.getConsentSignMethodJwt()
                : oidcProperties.getConsentSignMethod();
        return oidcProperties.getAcrValues() + method;
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
}
