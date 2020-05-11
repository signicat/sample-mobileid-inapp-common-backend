package com.signicat.demo.sampleapp.inapp.common.beans;

public class SuccessResponse extends BaseResponse {
    public SuccessResponse() {
        super();
    }

    public SuccessResponse(final Object successData) {
        super("SUCCESS", successData);
    }

}
