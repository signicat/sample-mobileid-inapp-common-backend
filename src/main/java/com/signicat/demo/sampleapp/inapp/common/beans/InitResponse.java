package com.signicat.demo.sampleapp.inapp.common.beans;

public class InitResponse {
    private String externalRef;
    private String deviceName;

    public InitResponse() {}

    public InitResponse(final String externalRef, final String deviceName) {
        this.externalRef = externalRef;
        this.deviceName = deviceName;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(final String externalRef) {
        this.externalRef = externalRef;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(final String deviceName) {
        this.deviceName = deviceName;
    }

}
