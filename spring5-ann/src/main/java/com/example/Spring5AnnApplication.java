package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Spring5AnnApplication {

    public static void main(String[] args) {
        SpringApplication.run(Spring5AnnApplication.class, args);
    }

    @Bean
    TomcatReactiveWebServerFactory tomcatReactiveWebServerFactory() {
        TomcatReactiveWebServerFactory factory = new TomcatReactiveWebServerFactory() {

            @Override
            protected void customizeConnector(org.apache.catalina.connector.Connector connector) {
                connector.setProperty("maxKeepAliveRequests", "-1");
                super.customizeConnector(connector);
            }
        };
        return factory;
    }

}
