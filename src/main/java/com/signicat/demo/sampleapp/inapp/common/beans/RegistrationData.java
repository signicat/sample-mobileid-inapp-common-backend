package com.signicat.demo.sampleapp.inapp.common.beans;

public class RegistrationData extends OperationData {
    private String deviceName;
    private String artifact;

    public RegistrationData() {}

    public RegistrationData(final String oidcBase, final String clientId, final String scope, final String redirectUri,
                            final String regMethod, final String state, final String externalRef, final String deviceName, final String artifact) {
        super(oidcBase, clientId, scope, redirectUri, regMethod, state, externalRef);
        this.externalRef = externalRef;
        this.deviceName = deviceName;
        this.artifact = artifact;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(final String deviceName) {
        this.deviceName = deviceName;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(final String artifact) {
        this.artifact = artifact;
    }
}
