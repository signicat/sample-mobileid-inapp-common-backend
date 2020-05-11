package com.signicat.demo.sampleapp.inapp.common.wsclient.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScidProperty {

    private String name;
    private String value;

    public ScidProperty(
            @JsonProperty("name") final String name,
            @JsonProperty("value") final String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
