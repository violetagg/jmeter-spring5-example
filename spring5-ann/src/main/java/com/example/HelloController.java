package com.example;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
final class HelloController {
    private final Random rnd = new Random();
    private final byte[] content = new byte[16384];
    private final ByteBuffer buffer = ByteBuffer.wrap(content);
    private final List<Long> list;

    HelloController() {
        rnd.nextBytes(content);
        list = Stream.iterate(1L, l -> l + 1).limit(30).collect(Collectors.toList());
    }

    @GetMapping(value = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    Mono<String> hello() {
        return Mono.just("hello");
    }

    @GetMapping(value = "/delay", produces = MediaType.TEXT_PLAIN_VALUE)
    Mono<ByteBuffer> delay(@RequestParam(required = false, defaultValue = "2000") long delayInterval) {
        return Mono.just(buffer).delayElement(Duration.ofMillis(delayInterval));
    }

    @GetMapping(value = "/json_interval", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    Flux<Map<String,Long>> json_interval(@RequestParam(required = false, defaultValue = "100") long delayInterval) {
        return Flux.interval(Duration.ofMillis(delayInterval))
                .map(l -> Collections.singletonMap("foo", l)).onBackpressureBuffer();
    }

    @GetMapping(value = "/json_list", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    Flux<Map<String,Long>> json_list(@RequestParam(required = false, defaultValue = "100") long delayInterval) {
        return Flux.fromIterable(list).delayElements(Duration.ofMillis(delayInterval))
                .map(l -> Collections.singletonMap("foo", l)).onBackpressureBuffer();
    }

}
