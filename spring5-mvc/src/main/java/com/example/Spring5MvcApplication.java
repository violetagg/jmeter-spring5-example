package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

    
    @Configuration
    static class WebConfig implements WebMvcConfigurer {

        @Override
        public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
            configurer.setTaskExecutor(mvcTaskExecutor());
        }

        @Bean
        public AsyncTaskExecutor mvcTaskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(32);
            executor.setAllowCoreThreadTimeOut(true);
            return executor;
        }
    }

}
