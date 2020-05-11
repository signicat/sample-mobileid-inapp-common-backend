package com.signicat.demo.sampleapp.inapp.common.beans;

public class ErrorResponse extends BaseResponse {
    public ErrorResponse() {
        super();
    }

    public ErrorResponse(final String errorMsg) {
        super("ERROR", errorMsg);
    }
}
