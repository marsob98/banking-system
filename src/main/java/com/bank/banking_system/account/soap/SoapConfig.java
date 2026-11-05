package com.bank.banking_system.account.soap;

import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SoapConfig {

    @Bean
    public Endpoint payeeVerificationSoapEndpoint(Bus bus, PayeeVerificationEndpoint implementor) {
        EndpointImpl endpoint = new EndpointImpl(bus, implementor);
        endpoint.publish("/payee-verification");
        return endpoint;
    }
}
