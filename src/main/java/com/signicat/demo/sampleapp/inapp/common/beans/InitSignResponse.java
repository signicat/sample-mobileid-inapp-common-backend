package com.signicat.demo.sampleapp.inapp.common.beans;

public class InitSignResponse extends InitResponse {
    private boolean signJwtMethodIsConfigured;

    public InitSignResponse() {}

    public InitSignResponse(final String externalRef, final String deviceName, final boolean signJwtMethodIsConfigured) {
        super(externalRef, deviceName);
        this.signJwtMethodIsConfigured = signJwtMethodIsConfigured;
    }

    public boolean getSignJwtMethodIsConfigured() {
        return signJwtMethodIsConfigured;
    }

    public void setSignJwtMethodIsConfigured(final boolean signJwtMethodIsConfigured) {
        this.signJwtMethodIsConfigured = signJwtMethodIsConfigured;
    }
}
