package com.signicat.demo.sampleapp.inapp.microservice.beans;

public class SignedDocument {

    protected String type;
    protected String data;

    public SignedDocument() { }

    public SignedDocument(final String type, final String data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}
