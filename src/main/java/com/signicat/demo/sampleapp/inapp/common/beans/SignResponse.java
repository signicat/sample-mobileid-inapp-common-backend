package com.signicat.demo.sampleapp.inapp.common.beans;

public class SignResponse extends OperationResponse{

    public SignResponse() {}

    public SignResponse(final String status, final String statusUrl, final String completeUrl, final OIDCErrorResponse error) {
        super(status, statusUrl, completeUrl, error);
    }

    public SignResponse(final OIDCErrorResponse error) {
        super(error);
    }

    @Override
    public String toString() {
        return "SignResponse{" +
                "status='" + status + '\'' +
                ", statusUrl='" + statusUrl + '\'' +
                ", completeUrl='" + completeUrl + '\'' +
                ", error=" + error +
                '}';
    }
}
