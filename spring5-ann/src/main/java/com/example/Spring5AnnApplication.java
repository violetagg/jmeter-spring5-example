package com.example;
/*
import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
*/
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatReactiveWebServerFactory;
import org.springframework.boot.web.embedded.undertow.UndertowReactiveWebServerFactory;
import org.springframework.boot.web.embedded.undertow.UndertowWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
/*
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceHandle;
*/
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
                System.out.println("maxKeepAliveRequests = -1");
                connector.setProperty("maxKeepAliveRequests", "-1");
                super.customizeConnector(connector);
            }
        };
        return factory;
    }

/*
    @Bean
    UndertowReactiveWebServerFactory undertowReactiveWebServerFactory() {
        UndertowReactiveWebServerFactory factory = new UndertowReactiveWebServerFactory() {

            private Integer bufferSize;
            private Integer ioThreads;
            private Integer workerThreads;
            private Boolean directBuffers;

            @Override
            public WebServer getWebServer(org.springframework.http.server.reactive.HttpHandler httpHandler) {
                Undertow.Builder builder = createBuilder(getPort(), httpHandler);
                return new UndertowWebServer(builder, getPort() >= 0);
            }

            private Undertow.Builder createBuilder(int port, org.springframework.http.server.reactive.HttpHandler httpHandler) {
                Undertow.Builder builder = getBuilder(httpHandler);
                if (this.bufferSize != null) {
                    builder.setBufferSize(this.bufferSize);
                }
                if (this.ioThreads != null) {
                    builder.setIoThreads(this.ioThreads);
                }
                if (this.workerThreads != null) {
                    builder.setWorkerThreads(this.workerThreads);
                }
                if (this.directBuffers != null) {
                    builder.setDirectBuffers(this.directBuffers);
                }
                builder.addHttpListener(port, getListenAddress());
                return builder;
            }

            private String getListenAddress() {
                if (getAddress() == null) {
                    return "0.0.0.0";
                }
                return getAddress().getHostAddress();
            }

            private Undertow.Builder getBuilder(org.springframework.http.server.reactive.HttpHandler httpHandler) {
                try {
                    DeploymentInfo servletBuilder = deployment()
                            .setClassLoader(this.getClass().getClassLoader())
                            .setContextPath("/")
                            .setDeploymentName("test.war")
                            .addServlets(servlet("Servlet", ServletHttpHandlerAdapter.class, () -> {
                                    return new InstanceHandle<Servlet>() {

                                        @Override
                                        public Servlet getInstance() {
                                            return new ServletHttpHandlerAdapter(httpHandler);
                                        }

                                        @Override
                                        public void release() {
                                            // no-op
                                        }
                                    };
                                })
                                .addMapping("/*")
                                .setAsyncSupported(true));

                    DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
                    manager.deploy();

                    HttpHandler servletHandler = manager.start();
                    return Undertow.builder().setHandler(servletHandler);
                } catch (ServletException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        return factory;
    }
*/
}
