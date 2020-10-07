package com.signicat.demo.sampleapp.inapp.common.beans;

public class SignStatusResponse extends StatusResponse {

    private String completeUri;
    private String errorMessage;
    private String errorMessageKey;
    private String errorUri;

    public SignStatusResponse() {
    }

    public String getCompleteUri() {
        return completeUri;
    }

    public void setCompleteUri(String completeUri) {
        this.completeUri = completeUri;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessageKey() {
        return errorMessageKey;
    }

    public void setErrorMessageKey(String errorMessageKey) {
        this.errorMessageKey = errorMessageKey;
    }

    public String getErrorUri() {
        return errorUri;
    }

    public void setErrorUri(String errorUri) {
        this.errorUri = errorUri;
    }
}
