package com.signicat.demo.sampleapp.inapp.common.beans;

public class RegistrationResponse extends OperationResponse {
    private String activationCode;

    public RegistrationResponse() {}

    public RegistrationResponse(final String activationCode, final String status, final String statusUrl, final String completeUrl,
            final OIDCErrorResponse error) {
        super(status, statusUrl, completeUrl, error);
        this.activationCode = activationCode;
    }

    public RegistrationResponse(final OIDCErrorResponse error) {
        super(error);
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(final String activationCode) {
        this.activationCode = activationCode;
    }

    @Override
    public String toString() {
        return "RegistrationResponse [activationCode=" + activationCode + ", status=" + status + ", statusUrl=" + statusUrl
                + ", completeUrl=" + completeUrl + ", error=" + error + "]";
    }

}
