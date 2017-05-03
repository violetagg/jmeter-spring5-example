package com.example.jmeter.plugin.reactive.sampler;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.function.Function;

import org.apache.jmeter.samplers.SampleResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.HttpResources;
import reactor.ipc.netty.http.client.HttpClient;
import reactor.ipc.netty.http.client.HttpClientResponse;
import reactor.ipc.netty.resources.PoolResources;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.client.reactive.ReactorClientHttpRequest;
import org.springframework.http.client.reactive.ReactorClientHttpResponse;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

final class ReactiveWebClient {

    static {
        HttpResources.set(PoolResources.elastic("elastic"));
    }

    SampleResult request(String method, String url, long sampleTimeout,
            long initialResponseReadDelay, long responseReadDelay, String accept) {
        ReactiveSampleResult sample = new ReactiveSampleResult();
        sample.setSampleLabel(url);

        InstrumentableClientConnector connector = new InstrumentableClientConnector();
        connector.setSampleResult(sample);
        connector.setDelay(initialResponseReadDelay);

        WebClient webClient = WebClient.builder().clientConnector(connector).build();

        sample.sampleStart();

        Mono<ClientResponse> response = getClientResponse(webClient, url, accept);

        Flux<ByteBuffer> body = getBody(response, sample);

        sample.setExecutionResult(readBody(body, sample, responseReadDelay, sampleTimeout));

        return sample;
    }

    private Mono<ClientResponse> getClientResponse(WebClient webClient, String url, String accept) {
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

    private Flux<ByteBuffer> getBody(Mono<ClientResponse> response, ReactiveSampleResult sample) {
        return response.doOnError(sample::errorResult)
                .flatMapMany(r -> {
                    int responseCode = r.statusCode().value();
                    sample.setResponseCode(responseCode + "");

                    sample.setSuccessful(isSuccessCode(responseCode));

                    return r.bodyToFlux(ByteBuffer.class);
                });
    }

    private Mono<Void> readBody(Flux<ByteBuffer> body, ReactiveSampleResult sample,
            long responseReadDelay, long sampleTimeout) {
        return delayReadingResponseElements(body, responseReadDelay)
                //.take(20)
                .limitRate(1)
                .reduce(0L, (i, j) -> i + j.remaining())
                .doOnError(sample::errorResult)
                .doOnNext(b -> {
                    sample.sampleEnd();
                    sample.setBodySize(b);
                })
                .then()
                .timeout(Duration.ofMillis(sampleTimeout));
    }

    private Flux<ByteBuffer> delayReadingResponseElements(Flux<ByteBuffer> source, long delay) {
        return delay > 0 ? source.delayElements(Duration.ofMillis(delay)) : source;
    }

    private boolean isSuccessCode(int code) {
        return code >= 200 && code <= 399;
    }

    private static final class InstrumentableClientConnector implements ClientHttpConnector {
        private final HttpClient httpClient;
        private SampleResult sample;
        private long delay;

        public InstrumentableClientConnector() {
            this.httpClient = HttpClient.create();
        }

        void setSampleResult(SampleResult sample) {
            this.sample = sample;
        }

        void setDelay(long delay) {
            this.delay = delay;
        }

        @Override
        public Mono<ClientHttpResponse> connect(HttpMethod method, URI uri,
                Function<? super ClientHttpRequest, Mono<Void>> requestCallback) {
            Mono<HttpClientResponse> response = httpClient
                    .request(io.netty.handler.codec.http.HttpMethod.valueOf(method.name()),
                            uri.toString(),
                            httpClientRequest -> {
                                this.sample.connectEnd();
                                return requestCallback
                                        .apply(new ReactorClientHttpRequest(method, uri, httpClientRequest));
                            });
            return delayReadingResponse(response)
                    .map(r -> {
                        this.sample.latencyEnd();
                        return new ReactorClientHttpResponse(r);
                    });
        }

        private Mono<HttpClientResponse> delayReadingResponse(Mono<HttpClientResponse> source) {
            return this.delay > 0 ? source.delayElement(Duration.ofMillis(this.delay)) : source;
        }
    }
}