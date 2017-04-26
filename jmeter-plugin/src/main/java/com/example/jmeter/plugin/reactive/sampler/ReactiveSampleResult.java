package com.example.jmeter.plugin.reactive.sampler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.jmeter.samplers.SampleResult;

import reactor.core.publisher.Mono;

public class ReactiveSampleResult extends SampleResult {

    private static final long serialVersionUID = 1L;
    private Mono<Void> executionResult;

    void setExecutionResult(Mono<Void> executionResult) {
        this.executionResult = executionResult;
    }

    Mono<Void> getExecutionResult() {
        return this.executionResult;
    }

    void errorResult(Throwable t) {
        if (getEndTime() == 0) {
            sampleEnd();
            setDataType("text");
            ByteArrayOutputStream text = new java.io.ByteArrayOutputStream(200);
            t.printStackTrace(new PrintStream(text));
            setResponseData(text.toByteArray());
            setResponseCode("Non HTTP response code: " + t.getClass().getName());
            setResponseMessage(text.toString());
        }
        setSuccessful(false);
    }
}
