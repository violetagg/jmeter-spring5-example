package com.example;

//import static io.undertow.servlet.Servlets.defaultContainer;
//import static io.undertow.servlet.Servlets.deployment;
//import static io.undertow.servlet.Servlets.servlet;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

//import javax.servlet.Servlet;
//import javax.servlet.ServletException;
//import io.undertow.Undertow;
//import io.undertow.server.HttpHandler;
//import io.undertow.servlet.api.DeploymentInfo;
//import io.undertow.servlet.api.DeploymentManager;
//import io.undertow.servlet.api.InstanceHandle;

@SpringBootApplication
public class Spring5FuncApplication {
    private final static Random rnd = new Random();
    private final static byte[] content = new byte[16384];
    private final ByteBuffer buffer = ByteBuffer.wrap(content);
    private static List<String> list;

    public static void main(String[] args) {
        rnd.nextBytes(content);
        list = Stream.iterate("foo ", l -> l + 1).limit(30).collect(Collectors.toList());
        SpringApplication.run(Spring5FuncApplication.class, args);
    }

    @Bean
    RouterFunction<?> routerFunction() {
        return route(GET("/hello"), request ->
                    ServerResponse.ok().body(Mono.just("hello"), String.class))
                .andRoute(GET("/delay"), request -> {
                    long delayInterval = Long.valueOf(request.queryParam("delayInterval").orElse("2000"));
                    return ServerResponse.ok()
                        .body(Mono.just(buffer).delayElement(Duration.ofMillis(delayInterval)),
                            ByteBuffer.class);
                })
                .andRoute(GET("/json_interval"), request -> {
                    long delayInterval = Long.valueOf(request.queryParam("delayInterval").orElse("100"));
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_STREAM_JSON)
                            .body(BodyInserters.fromPublisher(
                                    Flux.interval(Duration.ofMillis(delayInterval))
                                            .map(l -> Collections.singletonMap("foo", l))
                                            .onBackpressureDrop(),
                                    ResolvableType.forClassWithGenerics(Map.class, String.class, Long.class)));
                })
                .andRoute(GET("/json_list"), request -> {
                    long delayInterval = Long.valueOf(request.queryParam("delayInterval").orElse("100"));
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_STREAM_JSON)
                        .body(BodyInserters.fromPublisher(
                                Flux.fromIterable(list).delayElements(Duration.ofMillis(delayInterval))
                                        .map(l -> Collections.singletonMap("foo", l))
                                        .onBackpressureDrop(),
                                ResolvableType.forClassWithGenerics(Map.class, String.class, Long.class)));
                });
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
