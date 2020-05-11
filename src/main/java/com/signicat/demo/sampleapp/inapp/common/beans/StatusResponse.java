package com.signicat.demo.sampleapp.inapp.common.beans;

public class StatusResponse {
    private String status;

    public StatusResponse() {}

    public StatusResponse(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

}
