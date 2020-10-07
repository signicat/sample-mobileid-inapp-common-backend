package com.signicat.demo.sampleapp.inapp.microservice.beans;

public class BaseMicroserviceResponse {

    protected String externalRef;
    protected Object deviceProperties;

    public BaseMicroserviceResponse() {}

    public BaseMicroserviceResponse(final String externalRef, final Object deviceProperties) {
        this.externalRef = externalRef;
        this.deviceProperties = deviceProperties;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(String externalRef) {
        this.externalRef = externalRef;
    }

    public Object getDeviceProperties() {
        return deviceProperties;
    }

    public void setDeviceProperties(Object deviceProperties) {
        this.deviceProperties = deviceProperties;
    }
}
