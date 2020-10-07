package com.signicat.demo.sampleapp.inapp.microservice.beans;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

class TokenResponse {
    @JsonAlias("access_token")
    String accessToken;
    @JsonAlias("token_type")
    String tokenType;
    @JsonAlias("scope")
    String scope;
    @JsonAlias("expires_in")
    int expiresIn;

    public TokenResponse() {
    }

    public TokenResponse(final String accessToken, final String tokenType, final String scope, final int expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.scope = scope;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}
