package com.example.jmeter.plugin.reactive.sampler;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.function.Function;

import org.apache.jmeter.samplers.SampleResult;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.HttpResources;
import reactor.ipc.netty.resources.PoolResources;

final class ReactiveWebClient {
    private final WebClient webClient;
    private final InstrumentableClientConnector connector;

    static {
        HttpResources.set(PoolResources.elastic("elastic"));
    }

    ReactiveWebClient() {
        this.connector = new InstrumentableClientConnector();
        this.webClient = WebClient.builder().clientConnector(this.connector).build();
    }

    SampleResult request(String method, String url, long sampleTimeout,
            long initialResponseReadDelay, long responseReadDelay, String accept) {
        ReactiveSampleResult sample = new ReactiveSampleResult();
        this.connector.setSampleResult(sample);
        sample.setSampleLabel(url);
        sample.sampleStart();

        Mono<ClientResponse> response = getClientResponse(url, sample, accept);

        Flux<ByteBuffer> body = getBody(response, sample, initialResponseReadDelay);

        sample.setExecutionResult(readBody(body, sample, responseReadDelay, sampleTimeout));

        return sample;
    }

    private Mono<ClientResponse> getClientResponse(String url, SampleResult sample, String accept) {
        if (accept != null && !"".equals(accept)) {
            return webClient.get()
                    .uri(url)
                    .accept(MediaType.parseMediaType(accept))
                    .exchange();
        } else {
            return webClient.get()
                    .uri(url)
                    .exchange();
        }
    }

    private Flux<ByteBuffer> getBody(Mono<ClientResponse> response, ReactiveSampleResult sample,
            long initialResponseReadDelay) {
        return delayReadingResponse(response, initialResponseReadDelay)
                .otherwise(t -> {
                    sample.errorResult(t);
                    return Mono.error(t);
                })
                .flatMap(r -> {
                    sample.latencyEnd();
                    int responseCode = r.statusCode().value();
                    sample.setResponseCode(responseCode + "");

                    sample.setSuccessful(isSuccessCode(responseCode));

                    return r.bodyToFlux(ByteBuffer.class);
                })
                .take(20);
    }

    private Mono<Void> readBody(Flux<ByteBuffer> body, ReactiveSampleResult sample,
            long responseReadDelay, long sampleTimeout) {
        return delayReadingResponseElements(body, responseReadDelay)
                .reduce(0L, (i, j) -> i + j.remaining())
                .otherwise(t -> {
                    sample.errorResult(t);
                    return Mono.error(t);
                })
                .then(b -> {
                    sample.sampleEnd();
                    sample.setBodySize(b);
                    return Mono.empty();
                })
                .then()
                .timeout(Duration.ofMillis(sampleTimeout));
    }

    private Mono<ClientResponse> delayReadingResponse(Mono<ClientResponse> source, long delay) {
        return delay > 0 ? source.delayElement(Duration.ofMillis(delay)) : source;
    }

    private Flux<ByteBuffer> delayReadingResponseElements(Flux<ByteBuffer> source, long delay) {
        return delay > 0 ? source.delayElements(Duration.ofMillis(delay)) : source;
    }

    private boolean isSuccessCode(int code) {
        return code >= 200 && code <= 399;
    }

    private static final class InstrumentableClientConnector extends ReactorClientHttpConnector {
        private SampleResult sample;

        void setSampleResult(SampleResult sample) {
            this.sample = sample;
        }

        @Override
        public Mono<ClientHttpResponse> connect(HttpMethod method, URI uri,
                Function<? super ClientHttpRequest, Mono<Void>> requestCallback) {
            return super.connect(method, uri, request -> {
                this.sample.connectEnd();
                return requestCallback.apply(request);
            });
        }
    }
}