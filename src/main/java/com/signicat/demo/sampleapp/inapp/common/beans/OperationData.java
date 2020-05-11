package com.signicat.demo.sampleapp.inapp.common.beans;

public class OperationData {
    protected String authorizationCodeFlowBaseUrl;
    protected String clientId;
    protected String scope;
    protected String redirectUri;
    protected String methodName;
    protected String state;
    protected String externalRef;

    public OperationData() {}

    public OperationData(final String authorizationCodeFlowBaseUrl, final String clientId, final String scope, final String redirectUri,
                         final String methodName, final String state, final String externalRef) {
        this.authorizationCodeFlowBaseUrl = authorizationCodeFlowBaseUrl;
        this.clientId = clientId;
        this.scope = scope;
        this.redirectUri = redirectUri;
        this.methodName = methodName;
        this.state = state;
        this.externalRef = externalRef;
    }

    public void setClientId(final String id) {
        this.clientId = id;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setRedirectUri(final String uri) {
        this.redirectUri = uri;
    }

    public String getRedirectUri() {
        return this.redirectUri;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(final String methodName) {
        this.methodName = methodName;
    }

    public String getAuthorizationCodeFlowBaseUrl() {
        return authorizationCodeFlowBaseUrl;
    }

    public void setAuthorizationCodeFlowBaseUrl(final String authorizationCodeFlowBaseUrl) {
        this.authorizationCodeFlowBaseUrl = authorizationCodeFlowBaseUrl;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(final String externalRef) {
        this.externalRef = externalRef;
    }

}
