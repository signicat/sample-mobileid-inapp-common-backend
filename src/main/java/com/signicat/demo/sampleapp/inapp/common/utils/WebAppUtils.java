package com.signicat.demo.sampleapp.inapp.common.utils;

import java.io.IOException;
import java.util.Map;

import com.signicat.demo.sampleapp.inapp.common.beans.SignStatusResponse;
import com.signicat.demo.sampleapp.inapp.microservice.beans.BaseMicroserviceResponse;
import com.signicat.demo.sampleapp.inapp.microservice.beans.CompleteSignMicroserviceResponse;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.signicat.demo.sampleapp.inapp.common.beans.BaseResponse;
import com.signicat.demo.sampleapp.inapp.common.exception.ApplicationException;
import com.signicat.demo.sampleapp.inapp.common.beans.StatusResponse;

public class WebAppUtils {

    public static final String STATE_KEY                    = "STATE_KEY";
    public static final String AUTHORIZE_RESPONSE_TYPE_CODE = "authorize?response_type=code";

    public static SignStatusResponse checkSignStatus(final CloseableHttpClient httpClient, final String statusUrl) {
        final SignStatusResponse st =
                httpGet(httpClient, statusUrl, SignStatusResponse.class,
                        ImmutableMap.of(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString()));
        return st;
    }

    public static String checkStatus(final CloseableHttpClient httpClient, final String statusUrl) {
        final StatusResponse st =
                httpGet(httpClient, statusUrl, StatusResponse.class,
                        ImmutableMap.of(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString()));
        return st.getStatus();
    }

    public static BaseResponse doComplete(final CloseableHttpClient httpClient, final String completeUrl, final String stateKey) {
        return httpGet(httpClient, completeUrl, BaseResponse.class, ImmutableMap.of(STATE_KEY, stateKey));
    }

    public static String checkStatusMicroservice(final CloseableHttpClient httpClient, final String statusUrl, final String accessToken) {
        final StatusResponse st =
                httpGet(httpClient, statusUrl, StatusResponse.class,
                        ImmutableMap.of(
                                HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString(),
                                HttpHeaders.AUTHORIZATION, "Bearer " + accessToken
                        ));
        return st.getStatus();
    }

    public static BaseMicroserviceResponse doCompleteMicroservice(final CloseableHttpClient httpClient, final String completeUrl, final String accessToken) {
        return httpGet(httpClient, completeUrl, BaseMicroserviceResponse.class, ImmutableMap.of(
                HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString(),
                HttpHeaders.AUTHORIZATION, "Bearer " + accessToken
                ));
    }

    public static CompleteSignMicroserviceResponse doCompleteSignMicroservice(final CloseableHttpClient httpClient, final String completeUrl, final String accessToken) {
        return httpGet(httpClient, completeUrl, CompleteSignMicroserviceResponse.class, ImmutableMap.of(
                HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString(),
                HttpHeaders.AUTHORIZATION, "Bearer " + accessToken
        ));
    }

    public static <T> T httpGet(final CloseableHttpClient httpClient, final String url, final Class<T> clazz,
            final Map<String, String> headers) {
        final HttpGet httpGet = new HttpGet(url);
        for (final Map.Entry<String, String> me : headers.entrySet()) {
            httpGet.setHeader(me.getKey(), me.getValue());
        }

        String content = "";
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
            content = EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8);
            return new ObjectMapper().readValue(content, clazz);
        } catch (final IOException e) {
            throw new ApplicationException("HTTP GET to " + url + "failed. Error:" + e.getMessage());
        }
    }

    public static <T> T httpPost(final CloseableHttpClient httpClient, final String url, final Class<T> clazz,
                                final Map<String, String> headers) {
        final HttpGet httpGet = new HttpGet(url);
        for (final Map.Entry<String, String> me : headers.entrySet()) {
            httpGet.setHeader(me.getKey(), me.getValue());
        }

        String content = "";
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
            content = EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8);
            return new ObjectMapper().readValue(content, clazz);
        } catch (final IOException e) {
            throw new ApplicationException("HTTP POST to " + url + "failed. Error:" + e.getMessage());
        }
    }
}
