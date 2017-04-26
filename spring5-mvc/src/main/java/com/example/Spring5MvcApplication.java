package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Spring5MvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(Spring5MvcApplication.class, args);
    }

    @Bean
    TomcatServletWebServerFactory tomcatServletWebServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory() {

            @Override
            protected void customizeConnector(org.apache.catalina.connector.Connector connector) {
                connector.setProperty("maxKeepAliveRequests", "-1");
                super.customizeConnector(connector);
            }
        };
        return factory;
    }

}
