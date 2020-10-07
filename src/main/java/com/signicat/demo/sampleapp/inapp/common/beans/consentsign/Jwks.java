package com.signicat.demo.sampleapp.inapp.common.beans.consentsign;

import net.minidev.json.JSONObject;

import java.util.List;

public class Jwks {

    protected List<JSONObject> keys;

    public List<JSONObject>  getKeys() {
        return keys;
    }

    public void setKeys(List<JSONObject> keys) {
        this.keys = keys;
    }
}
