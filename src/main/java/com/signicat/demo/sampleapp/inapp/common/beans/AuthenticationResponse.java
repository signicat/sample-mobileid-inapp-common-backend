package com.signicat.demo.sampleapp.inapp.common.beans;

public class AuthenticationResponse extends OperationResponse {

    public AuthenticationResponse() {}

    public AuthenticationResponse(final String status, final String statusUrl, final String completeUrl, final OIDCErrorResponse error) {
        super(status, statusUrl, completeUrl, error);
    }

    public AuthenticationResponse(final OIDCErrorResponse error) {
        super(error);
    }

    @Override
    public String toString() {
        return "AuthenticationResponse [status=" + status + ", statusUrl=" + statusUrl + ", completeUrl=" + completeUrl + ", error=" + error
                + "]";
    }

}
