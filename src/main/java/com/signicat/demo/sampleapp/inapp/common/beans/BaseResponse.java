package com.signicat.demo.sampleapp.inapp.common.beans;

public class BaseResponse {

    protected String status;
    protected Object data;

    public BaseResponse() {}

    public BaseResponse(final String status, final Object data) {
        this.status = status;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(final Object data) {
        this.data = data;
    }
}
