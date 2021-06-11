package com.signicat.demo.sampleapp.inapp.common.wsclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import com.signicat.demo.sampleapp.inapp.common.wsclient.beans.ScidProperty;
import com.signicat.demo.sampleapp.inapp.common.wsclient.beans.ScidRequest;
import com.signicat.generated.scid.AddupdAccountProperties;
import com.signicat.generated.scid.AddupdAccountProperty;
import com.signicat.generated.scid.CreateAccountRequest;
import com.signicat.generated.scid.CreateArtifactRequest;
import com.signicat.generated.scid.DeleteDeviceRequest;
import com.signicat.generated.scid.GetAccountRequest;
import com.signicat.generated.scid.GetAccountResponse;
import com.signicat.generated.scid.GetDevicesRequest;
import com.signicat.generated.scid.ObjectFactory;

public class ScidWsClient extends WebServiceGatewaySupport {

    @Value("${ws.scidWsUrl}")
    protected String scidWsUrl;

    @Value("${ws.service}")
    protected String serviceName;

    @Value("${ws.apiKey}")
    protected String apiKey;

    @Value("${ws.domain}")
    protected String domain;

    private static final Logger LOG = LoggerFactory.getLogger(ScidWsClient.class);

    public Object createAccount(final ScidRequest scidRequest) {
        final ObjectFactory objectFactory = new ObjectFactory();

        // create request object
        final CreateAccountRequest request = objectFactory.createCreateAccountRequest();
        request.setService(serviceName);
        request.setPassword(apiKey);
        request.setDomain(domain);
        request.setExternalReference(scidRequest.getExternalRef());
        // ------------------------------------------------------------------------
        // Note! Important! It is crucial that activated property is set to TRUE
        // ------------------------------------------------------------------------
        request.setActivated(true);

        // create properties and add to request
        final AddupdAccountProperties props = objectFactory.createAddupdAccountProperties();
        for (final ScidProperty scidProperty : scidRequest.getAccountProps()) {
            final AddupdAccountProperty prop = objectFactory.createAddupdAccountProperty();
            prop.setName(scidProperty.getName());
            prop.setValue(scidProperty.getValue());
            props.getProperty().add(prop);
        }
        request.setProperties(props);

        // call WS and return result
        return getWebServiceTemplate().marshalSendAndReceive(scidWsUrl, request);
    }

    public boolean accountExists(final String accountName) {
        try {
            final ObjectFactory objectFactory = new ObjectFactory();
            final GetAccountRequest request = objectFactory.createGetAccountRequest();
            request.setService(serviceName);
            request.setPassword(apiKey);
            request.setDomain(domain);
            request.setExternalReference(accountName);
            LOG.info("Checking if account exists for :" + accountName + " against " +scidWsUrl + " with service/domain " + serviceName + "/" +domain);
            final GetAccountResponse response = (GetAccountResponse) getWebServiceTemplate().marshalSendAndReceive(scidWsUrl, request);
            if (response.getAccount() != null && response.getAccount().getExternalReference().equalsIgnoreCase(accountName)) {
                return true;
            }
        } catch (final Exception e) {
            return false;
        }
        return false;
    }

    public Object createArtifact(final String externalRef) {
        final ObjectFactory objectFactory = new ObjectFactory();
        final CreateArtifactRequest request = objectFactory.createCreateArtifactRequest();
        request.setService(serviceName);
        request.setPassword(apiKey);
        request.setDomain(domain);
        request.setExternalReference(externalRef);
        return getWebServiceTemplate().marshalSendAndReceive(scidWsUrl, request);
    }

    public Object deleteDevice(final String externalRef, final String deviceName) {
        final ObjectFactory objectFactory = new ObjectFactory();
        final DeleteDeviceRequest request = objectFactory.createDeleteDeviceRequest();
        request.setService(serviceName);
        request.setPassword(apiKey);
        request.setDomain(domain);
        request.setDeviceName(deviceName);
        request.setExternalReference(externalRef);
        return getWebServiceTemplate().marshalSendAndReceive(scidWsUrl, request);
    }

    public Object getDevices(final String externalRef) {
        final ObjectFactory objectFactory = new ObjectFactory();
        final GetDevicesRequest request = objectFactory.createGetDevicesRequest();
        request.setService(serviceName);
        request.setDomain(domain);
        request.setPassword(apiKey);
        request.setExternalReference(externalRef);
        return getWebServiceTemplate().marshalSendAndReceive(scidWsUrl, request);
    }

}
