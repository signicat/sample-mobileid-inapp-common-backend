package com.signicat.demo.sampleapp.inapp.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("oidc")
public class OIDCProperties {
    private String oidcBase;
    private String clientId;
    private String scope;
    private String redirectUri;
    // private String deviceRedirectUri;
    private String acrValues;
    private String cred64;
    private String regMethod;
    private String authMethod;
    private String consentSignMethod;

    public OIDCProperties() {}

    /*
    public OIDCProperties(final String oidcBase, final String clientId, final String scope, final String redirectUri,
            final String acrValues, final String cred64, final String regMethod, final String authMethod) {
        this.oidcBase = oidcBase;
        this.clientId = clientId;
        this.scope = scope;
        this.redirectUri = redirectUri;
        this.acrValues = acrValues;
        this.cred64 = cred64;
        this.regMethod = regMethod;
        this.authMethod = authMethod;
    }
    */

    public void setOidcBase(final String oidcBase) {
        this.oidcBase = oidcBase;
    }

    public String getOidcBase() {
        return oidcBase;
    }

    public String getIssuerId() {
        return this.oidcBase;
    }

    public void setClientId(final String id) {
        this.clientId = id;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return this.scope;
    }

    public void setRedirectUri(final String uri) {
        this.redirectUri = uri;
    }

    public String getRedirectUri() {
        return this.redirectUri;
    }

    //public String getDeviceRedirectUri() { return deviceRedirectUri; }

    //public void setDeviceRedirectUri(String deviceRedirectUri) { this.deviceRedirectUri = deviceRedirectUri; }

    public void setAcrValues(final String acrValues) {
        this.acrValues = acrValues;
    }

    public String getAcrValues() {
        return this.acrValues;
    }

    public String getCred64() {
        return cred64;
    }

    public void setCred64(final String cred64) {
        this.cred64 = cred64;
    }

    public String getRegMethod() {
        return regMethod;
    }

    public void setRegMethod(final String regMethod) {
        this.regMethod = regMethod;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(final String authMethod) {
        this.authMethod = authMethod;
    }

    public String getConsentSignMethod() { return consentSignMethod; }

    public void setConsentSignMethod(String consentSignMethod) { this.consentSignMethod = consentSignMethod; }
}
