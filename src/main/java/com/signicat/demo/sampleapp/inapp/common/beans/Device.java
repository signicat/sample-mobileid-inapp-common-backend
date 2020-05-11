package com.signicat.demo.sampleapp.inapp.common.beans;

public class Device {
    final String deviceId;
    final String name;
    final String manufacturer;
    final String model;
    final String inputMethod;
    final String osFingerprint;
    final String osType;
    final String osVersion;
    final String rooted;
    final String debugEnabled;
    final String debugConnected;
    final String authMethods;

    private Device(final Builder builder) {
        deviceId = builder.deviceId;
        name = builder.name;
        manufacturer = builder.manufacturer;
        model = builder.model;
        inputMethod = builder.inputMethod;
        osFingerprint = builder.osFingerprint;
        osType = builder.osType;
        osVersion = builder.osVersion;
        rooted = builder.rooted;
        debugEnabled = builder.debugEnabled;
        debugConnected = builder.debugConnected;
        authMethods = builder.authMethods;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getModel() {
        return model;
    }

    public String getName() {
        return name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getInputMethod() {
        return inputMethod;
    }

    public String getOsFingerprint() {
        return osFingerprint;
    }

    public String getOsType() {
        return osType;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getRooted() {
        return rooted;
    }

    public String getDebugEnabled() {
        return debugEnabled;
    }

    public String getDebugConnected() {
        return debugConnected;
    }

    public String getAuthMethods() {
        return authMethods;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Device{");
        sb.append("deviceId='").append(deviceId).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", manufacturer='").append(manufacturer).append('\'');
        sb.append(", model='").append(model).append('\'');
        sb.append(", inputMethod='").append(inputMethod).append('\'');
        sb.append(", osFingerprint='").append(osFingerprint).append('\'');
        sb.append(", osType='").append(osType).append('\'');
        sb.append(", osVersion='").append(osVersion).append('\'');
        sb.append(", rooted='").append(rooted).append('\'');
        sb.append(", debugEnabled='").append(debugEnabled).append('\'');
        sb.append(", debugConnected='").append(debugConnected).append('\'');
        sb.append(", authMethods='").append(authMethods).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder {
        private String deviceId;
        private String name;
        private String manufacturer;
        private String model;
        private String inputMethod;
        private String osFingerprint;
        private String osType;
        private String osVersion;
        private String rooted;
        private String debugEnabled;
        private String debugConnected;
        private String authMethods;

        public Builder() {
        }

        public Builder deviceId(final String val) {
            deviceId = val;
            return this;
        }

        public Builder name(final String val) {
            name = val;
            return this;
        }

        public Builder manufacturer(final String val) {
            manufacturer = val;
            return this;
        }

        public Builder model(final String val) {
            model = val;
            return this;
        }

        public Builder inputMethod(final String val) {
            inputMethod = val;
            return this;
        }

        public Builder osFingerprint(final String val) {
            osFingerprint = val;
            return this;
        }

        public Builder osType(final String val) {
            osType = val;
            return this;
        }

        public Builder osVersion(final String val) {
            osVersion = val;
            return this;
        }

        public Builder rooted(final String val) {
            rooted = val;
            return this;
        }

        public Builder debugEnabled(final String val) {
            debugEnabled = val;
            return this;
        }

        public Builder debugConnected(final String val) {
            debugConnected = val;
            return this;
        }

        public Builder authMethods(final String val) {
            authMethods = val;
            return this;
        }

        public Device build() {
            return new Device(this);
        }
    }
}
