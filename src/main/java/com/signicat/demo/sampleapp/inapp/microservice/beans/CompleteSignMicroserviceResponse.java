package com.signicat.demo.sampleapp.inapp.microservice.beans;

public class CompleteSignMicroserviceResponse extends BaseMicroserviceResponse {
    protected SignedDocument  signature;

    public CompleteSignMicroserviceResponse() {}

    public CompleteSignMicroserviceResponse(String externalRef, Object deviceProperties, SignedDocument signature) {
        super(externalRef, deviceProperties);
        this.signature = signature;
    }

    public SignedDocument getSignature() {
        return signature;
    }

    public void setSignature(SignedDocument signature) {
        this.signature = signature;
    }
}
