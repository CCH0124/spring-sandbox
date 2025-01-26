package org.reactor.sec07;

import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class ThreadWithSchedulerTest {

    private static final Logger log = LoggerFactory.getLogger(ThreadWithSchedulerTest.class);

    @Test
    public void default_thread_test() {
        var flux = Flux.create(sink -> {
            for (int i = 1; i < 3; i++) {
                log.info("generating: {}", i);
                sink.next(i);
            }
            sink.complete();
        });
        // main thread
        flux.subscribe(x -> log.info("received {}", x));
    }

    @Test
    public void subscribeOn_boundedElastic_test() {
        var flux = Flux.create(sink -> {
            for (int i = 1; i < 3; i++) {
                log.info("generating: {}", i); // boundedElastic-1
                sink.next(i);
            }
            sink.complete();
        });
        // main thread
        flux
        .doFirst(() -> log.info("first1"))  // boundedElastic-1
        .subscribeOn(Schedulers.boundedElastic())
        .doFirst(() -> log.info("first2")) // main thread
        .subscribe(x -> log.info("received {}", x)); // boundedElastic-1
    }

    @Test
    public void test_01() {
        Flux<Object> flux = Flux.create(fluxSink -> {
            for (int i = 0; i < 5; i++) {
                fluxSink.next(i);
            }
            fluxSink.complete();
        })
        .doFirst(() -> log.debug("first")) // boundedElastic-1
        .subscribeOn(Schedulers.boundedElastic());
        
        flux.subscribeOn(Schedulers.parallel())
            .doFirst(() -> log.debug("first2")) // [main]
            .map(i -> i + "a") // boundedElastic-1
            .subscribe(x -> log.info("received: {}", x));
        /**
        [main] DEBUG reactor.util.Loggers - Using Slf4j logging framework
        [main] DEBUG o.r.sec07.ThreadWithSchedulerTest - first2
        [boundedElastic-1] DEBUG o.r.sec07.ThreadWithSchedulerTest - first
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 0a
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 1a
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 2a
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 3a
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 4a
         */
    }

    @Test
    public void test_02() {
        Flux.create(fluxSink -> {
            for (int i = 0; i < 5; i++) {
                fluxSink.next(i);
            }
            fluxSink.complete();
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doFirst(() -> log.info("first")) // parallel-1
        .subscribeOn(Schedulers.parallel())
        .map(i -> i + "a")
        .subscribe(x -> log.info("received: {}", x)); // boundedElastic-1
        /**
         [parallel-1] INFO  o.r.sec07.ThreadWithSchedulerTest - first
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 0a
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 1a
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 2a
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 3a
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 4a
         */
    }

    @Test
    public void test_03() {
        Flux.create(fluxSink -> {
            for (int i = 0; i < 5; i++) {
                fluxSink.next(i);
            }
            fluxSink.complete();
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doFirst(() -> log.info("first")) //  [main] 
        .publishOn(Schedulers.parallel())
        .map(i -> i + "a")
        .subscribe(x -> log.info("received: {}", x));
        /**
        [main] INFO  o.r.sec07.ThreadWithSchedulerTest - first
        [parallel-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 0a
        [parallel-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 1a
        [parallel-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 2a
        [parallel-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 3a
        [parallel-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 4a
         */
    }

    @Test
    public void test_04() {
        Flux.create(fluxSink -> {
            for (int i = 0; i < 5; i++) {
                fluxSink.next(i);
            }
            fluxSink.complete();
        })
        .publishOn(Schedulers.boundedElastic())
        .doFirst(() -> log.info("first"))
        .subscribeOn(Schedulers.parallel())
        .map(i -> i + "a")
        .subscribe(x -> log.info("received: {}", x));
        /**
         [parallel-1] INFO  o.r.sec07.ThreadWithSchedulerTest - first
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 0a
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 1a
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 2a
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 3a
        [boundedElastic-1] INFO  o.r.sec07.ThreadWithSchedulerTest - received: 4a
         */
    }
}
