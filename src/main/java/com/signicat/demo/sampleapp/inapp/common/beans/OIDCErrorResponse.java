package com.signicat.demo.sampleapp.inapp.common.beans;

import com.fasterxml.jackson.annotation.JsonAlias;

public class OIDCErrorResponse {
    @JsonAlias("error")
    private String code;
    @JsonAlias("error_description")
    private String message;

    public OIDCErrorResponse() {}

    public OIDCErrorResponse(final String code, final String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

}
