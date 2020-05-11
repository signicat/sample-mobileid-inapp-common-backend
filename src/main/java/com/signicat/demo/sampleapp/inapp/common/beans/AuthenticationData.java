package com.signicat.demo.sampleapp.inapp.common.beans;

public class AuthenticationData extends OperationData {
    private String deviceId;
    private String preContextTitle;

    public AuthenticationData() {}

    public AuthenticationData(final String oidcBase, final String clientId, final String scope, final String redirectUri,
                              final String authMethod, final String state, final String extRef, final String deviceId) {
        super(oidcBase, clientId, scope, redirectUri, authMethod, state, extRef);
        this.deviceId = deviceId;
    }

    public AuthenticationData(final String oidcBase, final String clientId, final String scope, final String redirectUri,
                              final String authMethod, final String state, final String extRef, final String deviceId,
                              final String preContextTitle) {
        super(oidcBase, clientId, scope, redirectUri, authMethod, state, extRef);
        this.deviceId = deviceId;
        this.preContextTitle = preContextTitle;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(final String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPreContextTitle() {
        return preContextTitle;
    }

    public void setPreContextTitle(String preContextTitle) {
        this.preContextTitle = preContextTitle;
    }
}
