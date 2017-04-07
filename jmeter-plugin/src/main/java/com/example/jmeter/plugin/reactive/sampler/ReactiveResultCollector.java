package com.example.jmeter.plugin.reactive.sampler;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class ReactiveResultCollector extends ResultCollector {

    private static final long serialVersionUID = 1L;
    private static final AtomicIntegerFieldUpdater<ReactiveResultCollector> COUNTER_UPDATER = AtomicIntegerFieldUpdater
            .newUpdater(ReactiveResultCollector.class, "counter");
    private volatile int counter = 0;

    public ReactiveResultCollector() {
        this(new Summariser("summariser"));
    }

    public ReactiveResultCollector(Summariser summariser) {
        super(summariser);
    }

    @Override
    public void sampleOccurred(SampleEvent event) {
        ReactiveSampleResult result = (ReactiveSampleResult) event.getResult();
        COUNTER_UPDATER.getAndIncrement(this);
        result.getExecutionResult().subscribe(new Subscriber<Void>() {

            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Void value) {
                // do nothing
            }

            @Override
            public void onComplete() {
                ReactiveResultCollector.super.sampleOccurred(event);
                COUNTER_UPDATER.getAndDecrement(ReactiveResultCollector.this);
            }

            @Override
            public void onError(Throwable throwable) {
                result.errorResult(throwable);
                ReactiveResultCollector.super.sampleOccurred(event);
                COUNTER_UPDATER.getAndDecrement(ReactiveResultCollector.this);
            }
        });
    }

    @Override
    public void testEnded(String host) {
        System.out.println("samples left: " + counter);
        /*
        while (counter > 0) {
            System.out.println("samples left: " + counter);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        */
        super.testEnded(host);
    }

}
