package com.signicat.demo.sampleapp.inapp.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.signicat.demo.sampleapp.inapp.common.OIDCProperties;
import com.signicat.demo.sampleapp.inapp.common.beans.AuthenticationData;
import com.signicat.demo.sampleapp.inapp.common.beans.OperationData;
import com.signicat.demo.sampleapp.inapp.common.beans.RegistrationData;
import com.signicat.demo.sampleapp.inapp.common.exception.ApplicationException;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OIDCUtils {

    public static final int    STATE_NUM_CHARS              = 8;
    public static final String STATE_CHOICES                = "ABCDEF0123456789";
    public static final String STATE_DELIM                  = ":";

    public static final String ORIG_STATE                   = "orig_state";
    public static final String AUTHORIZE_RESPONSE_TYPE_CODE = "authorize?response_type=code";

    public static final String WEB_CHANNEL                  = "WEB_CHANNEL";
    public static final String INAPP_CHANNEL                = "INAPP_CHANNEL";
    public static final String SIGN_CHANNEL                = "SIGN_CHANNEL";

    public static final String JWKS_PATH                = "jwks.json";

    public static String createState(final String base) {
        return base + STATE_DELIM + getRandomChars(STATE_NUM_CHARS, STATE_CHOICES);
    }

    private static String getRandomChars(final int numChars, final String selectFrom) {
        final StringBuilder sb = new StringBuilder(numChars);
        final Random random = new Random();
        final int len = selectFrom.length();
        for (int i = 0; i < numChars; i++) {
            final int index = random.nextInt(len);
            sb.append(selectFrom.charAt(index));
        }
        return sb.toString();
    }

    public static String getAuthorizeUri(final OperationData operationData) {
        final StringBuilder sb = new StringBuilder();
        sb.append(operationData.getAuthorizationCodeFlowBaseUrl());
        sb.append("&scope=").append(operationData.getScope());
        sb.append("&client_id=").append(operationData.getClientId());
        sb.append("&redirect_uri=").append(operationData.getRedirectUri());
        sb.append("&state=").append(operationData.getState());
        sb.append("&acr_values=").append(operationData.getMethodName());
        sb.append("&login_hint=externalRef-").append(operationData.getExternalRef());

        if (operationData instanceof AuthenticationData) {
            sb.append("&login_hint=deviceId-").append(((AuthenticationData) operationData).getDeviceId());

            if(((AuthenticationData) operationData).getPreContextTitle() != null && !((AuthenticationData) operationData).getPreContextTitle().isEmpty()){
                sb.append("&login_hint=preContextTitle-").append(urlEncodeValue(((AuthenticationData) operationData).getPreContextTitle()));
            }

            // test context data
            //sb.append("&login_hint=preContextTitle-").append("testPreContextTitle");
            //sb.append("&login_hint=preContextMessage-").append("testPreContextMessage");
            //sb.append("&login_hint=postContextTitle-").append("testPostContextTitle");
            //sb.append("&login_hint=postContextMessage-").append("testPostContextMessage");
        }
        else if (operationData instanceof RegistrationData) {
            if (((RegistrationData) operationData).getDeviceName() != null) {
                sb.append("&login_hint=deviceName-").append(((RegistrationData) operationData).getDeviceName());
            }
            sb.append("&login_hint=artifact-").append(((RegistrationData) operationData).getArtifact());
        }
        return sb.toString();
    }

    public static JSONObject getToken(final CloseableHttpClient httpClient, final OIDCProperties oidc, final String code) {
        final HttpPost httpPost = new HttpPost(oidc.getIssuerId() + "token");
        httpPost.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + oidc.getCred64());
        httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());

        final List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("client_id", oidc.getClientId()));
        nameValuePairs.add(new BasicNameValuePair("redirect_uri", oidc.getRedirectUri()));
        nameValuePairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
        nameValuePairs.add(new BasicNameValuePair("code", code));
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, StandardCharsets.UTF_8));

        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
            final String content = EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8);
            return new ObjectMapper().readValue(content, JSONObject.class);
        } catch (final IOException e) {
            throw new ApplicationException("Fetching token failed: " + e.getMessage());
        }
    }

    // Method to encode a string value using `UTF-8` encoding scheme
    private static String urlEncodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
