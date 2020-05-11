package com.signicat.demo.sampleapp.inapp.common.wsclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class ScidClientConfig {

    @Value("${ws.scidWsUrl}")
    protected String scidWsUrl;

    @Bean
    public Jaxb2Marshaller marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.signicat.generated.scid");
        return marshaller;
    }

    @Bean
    public ScidWsClient soapConnector(final Jaxb2Marshaller marshaller) {
        final ScidWsClient client = new ScidWsClient();
        client.setDefaultUri(scidWsUrl);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }
}
