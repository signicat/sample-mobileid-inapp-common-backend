package com.signicat.demo.sampleapp.inapp.common.wsclient.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ScidRequest {

    private String             externalRef;
    private List<ScidProperty> accountProps;

    public ScidRequest(
            @JsonProperty("externalRef") final String externalRef,
            @JsonProperty("accountProps") final ArrayList<ScidProperty> accountProps) {
        super();
        this.externalRef = externalRef;
        this.accountProps = accountProps;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(final String externalRef) {
        this.externalRef = externalRef;
    }

    public List<ScidProperty> getAccountProps() {
        return accountProps;
    }

    public void setAccountProps(final List<ScidProperty> accountProps) {
        this.accountProps = accountProps;
    }
}
