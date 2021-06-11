package com.signicat.demo.sampleapp.inapp.common;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.signicat.demo.sampleapp.inapp.common.beans.AuthenticationResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.RegistrationResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.SignResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.SignStatusResponse;
import com.signicat.generated.scid.Devices;

@Component
@SessionScope
public class SessionData {

    private final CloseableHttpClient httpClient =
            HttpClientBuilder.create().useSystemProperties().build();

    private String                    extRef;
    private String                    devName;
    private String                    activationCode;
    private RegistrationResponse      regResponse;
    private AuthenticationResponse    authResponse;
    private String                    stateKey;
    private Devices                   fetchedDevices;
    private SignResponse              signResponse;
    private SignStatusResponse        signStatusResponse;

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

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(final String activationCode) {
        this.activationCode = activationCode;
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
    public SignResponse getSignResponse() { return signResponse; }

    public void setSignResponse(final SignResponse signResponse) { this.signResponse = signResponse; }

    public SignStatusResponse getSignStatusResponse() { return signStatusResponse; }

    public void setSignStatusResponse(final SignStatusResponse signStatusResponse) {
        this.signStatusResponse = signStatusResponse;
    }

}
