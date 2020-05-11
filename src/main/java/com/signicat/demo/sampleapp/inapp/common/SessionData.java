package com.signicat.demo.sampleapp.inapp.common;

import com.signicat.demo.sampleapp.inapp.common.beans.AuthenticationResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.RegistrationResponse;
import com.signicat.generated.scid.Devices;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class SessionData {

    private final CloseableHttpClient httpClient =
            HttpClientBuilder.create().useSystemProperties().build();

    private String                    extRef;
    private String                    devName;
    private RegistrationResponse      regResponse;
    private AuthenticationResponse    authResponse;
    private String                    stateKey;
    private Devices                   fetchedDevices;

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public String getExtRef() {
        return extRef;
    }

    public String getDevName() {
        return devName;
    }

    public void setExtRef(final String extRef) {
        this.extRef = extRef;
    }

    public void setDevName(final String devName) {
        this.devName = devName;
    }

    public RegistrationResponse getRegResponse() {
        return regResponse;
    }

    public void setRegResponse(final RegistrationResponse regResponse) {
        this.regResponse = regResponse;
    }

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(final String stateKey) {
        this.stateKey = stateKey;
    }

    public Devices getFetchedDevices() {
        return fetchedDevices;
    }

    public void setFetchedDevices(final Devices fetchedDevices) {
        this.fetchedDevices = fetchedDevices;
    }

    public AuthenticationResponse getAuthResponse() {
        return authResponse;
    }

    public void setAuthResponse(final AuthenticationResponse authResponse) {
        this.authResponse = authResponse;
    }

}
