package com.example;

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

@SpringBootApplication
public class Spring5FuncApplication {
    private final static Random rnd = new Random();
    private final static byte[] content = new byte[16384];
    private final ByteBuffer buffer = ByteBuffer.wrap(content);
    private static List<Long> list;

    public static void main(String[] args) {
        rnd.nextBytes(content);
        list = Stream.iterate(1L, l -> l + 1).limit(20).collect(Collectors.toList());
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
                                            .onBackpressureBuffer(),
                                    ResolvableType.forClassWithGenerics(Map.class, String.class, Long.class)));
                })
                .andRoute(GET("/json_list"), request -> {
                    long delayInterval = Long.valueOf(request.queryParam("delayInterval").orElse("100"));
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_STREAM_JSON)
                        .body(BodyInserters.fromPublisher(
                                Flux.fromIterable(list).delayElements(Duration.ofMillis(delayInterval))
                                        .map(l -> Collections.singletonMap("foo", l))
                                        .onBackpressureBuffer(),
                                ResolvableType.forClassWithGenerics(Map.class, String.class, Long.class)));
                });
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
