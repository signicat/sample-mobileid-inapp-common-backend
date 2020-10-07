package com.signicat.demo.sampleapp.inapp.microservice.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.signicat.demo.sampleapp.inapp.common.OIDCProperties;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.http.util.EntityUtils;
import com.google.common.base.Charsets;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AccessTokenFetcher {

    // expiry margin in seconds
    private static final long                         EXPIRY_MARGIN    = 10L;

    private static final Logger                       LOG              =
            LogManager.getLogger(AccessTokenFetcher.class.getName());

    private static Cache<String, ExpiringAccessToken> accessTokenCache = CacheBuilder.newBuilder().build();

    private final String                              scope;
    private final String                              tokenCacheKey;
    private final OIDCProperties                      oidcProperties;

    public AccessTokenFetcher(final OIDCProperties oidcProperties) {
        this.scope = "client.mobileid";
        this.oidcProperties = oidcProperties;
        tokenCacheKey = oidcProperties.getClientId() + "@" + this.scope;
    }

    /**
     * Either get accessToken from cache (if it exists, is not expired and will not be in next {@link #EXPIRY_MARGIN} seconds)<br>
     * or create a new one and store it in cache
     *
     * @return accessToken
     * @throws IOException if something went wrong while fetching new access token
     */
    public String getValidAccessToken() throws IOException {
        final ExpiringAccessToken cachedToken = accessTokenCache.getIfPresent(tokenCacheKey);
        if (cachedAccessTokenIsValid(cachedToken)) {
            return cachedToken.getAccessToken();
        }
        return getNewAccessToken();
    }

    private boolean cachedAccessTokenIsValid(final ExpiringAccessToken cachedToken) {
        return (cachedToken != null && !cachedToken.isExpired(EXPIRY_MARGIN));
    }

    public String getNewAccessToken() throws IOException {
        return getAndCacheAccessToken();
    }

    private String getAndCacheAccessToken() throws IOException {
        LOG.trace("Obtaining New Access Token");
        final HttpPost httpPost = prepareAccessTokenRequest();
        final Map<String, Object> tokenResponse = fetchAccessTokenAsMap(httpPost);
        final int expiresIn = (int) tokenResponse.get("expires_in");
        final String accessToken = (String) tokenResponse.get("access_token");
        accessTokenCache.put(tokenCacheKey, new ExpiringAccessToken(Instant.now().plusSeconds(expiresIn), accessToken));
        return accessToken;
    }

    private HttpPost prepareAccessTokenRequest() {
        final HttpPost httpPost = new HttpPost(oidcProperties.getIssuerId() + "token");
        httpPost.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + oidcProperties.getCred64());

        final List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("grant_type", "client_credentials"));
        nameValuePairs.add(new BasicNameValuePair("scope", this.scope));
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, StandardCharsets.UTF_8));
        return httpPost;
    }

    private Map<String, Object> fetchAccessTokenAsMap(final HttpRequestBase request) throws IOException {
        final String requestURI = request.getURI().toASCIIString();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().useSystemProperties().build();
             CloseableHttpResponse httpResponse = httpClient.execute(request)) {

            final String content = EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8);
            final int responseCode = httpResponse.getStatusLine().getStatusCode();
            if (responseCode / 100 != 2) {
                final String message = String.format("Received an HTTP: %s and message: %s", responseCode, content);
                throw new IOException("URL: " + requestURI + " - " + message);
            }
            return new ObjectMapper().readValue(content, new TypeReference<Map<String, Object>>() {});

        } catch (final IOException e) {
            throw new IOException("Fetching JSON from Server URI " + requestURI + " failed.", e);
        }
    }

    class ExpiringAccessToken {

        private final Instant expiryTime;
        private final String  accessToken;

        ExpiringAccessToken(final Instant expiryTime, final String accessToken) {
            this.expiryTime = expiryTime;
            this.accessToken = accessToken;
        }

        public boolean isExpired() {
            return expiryTime.isBefore(Instant.now());
        }

        public boolean isExpired(final long secondsBeforeExpiry) {
            return expiryTime.isBefore(Instant.now().plusSeconds(secondsBeforeExpiry));
        }

        public String getAccessToken() {
            return accessToken;
        }
    }
}
