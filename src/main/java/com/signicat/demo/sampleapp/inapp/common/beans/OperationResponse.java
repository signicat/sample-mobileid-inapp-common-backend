package com.signicat.demo.sampleapp.inapp.common.beans;

public class OperationResponse {
    protected String            status;
    protected String            statusUrl;
    protected String            completeUrl;
    protected OIDCErrorResponse error;

    public OperationResponse() {}

    public OperationResponse(final String status, final String statusUrl, final String completeUrl, final OIDCErrorResponse error) {
        this.status = status;
        this.statusUrl = statusUrl;
        this.completeUrl = completeUrl;
        this.error = error;
    }

    public OperationResponse(final OIDCErrorResponse error) {
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getCompleteUrl() {
        return completeUrl;
    }

    public void setCompleteUrl(final String completeUrl) {
        this.completeUrl = completeUrl;
    }

    public String getStatusUrl() {
        return statusUrl;
    }

    public void setStatusUrl(final String statusUrl) {
        this.statusUrl = statusUrl;
    }

    public OIDCErrorResponse getError() {
        return error;
    }

    public void setError(final OIDCErrorResponse error) {
        this.error = error;
    }

}
