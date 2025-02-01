package org.reactor.sec12;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

public class SinkTest {
    private static final Logger log = LoggerFactory.getLogger(SinkTest.class);
    @Test
    public void sink_test01() {
        var sink = Sinks.one();
        var mono = sink.asMono();
        sink.tryEmitValue("hi");

        mono.subscribe(x -> log.info("{}", x));

        StepVerifier.create(mono).expectNext("hi").verifyComplete();

        sink.tryEmitEmpty();

        mono.subscribe(x -> log.info("{}", x));
    }

    @Test
    public void sink_test02() {
        var sink = Sinks.one();
        var mono = sink.asMono();
        sink.tryEmitError(new IllegalArgumentException("oops"));
        StepVerifier.create(mono).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    public void sink_test03() {
        /*
         * 只允許被喔一個人訂閱
         */
        var sink = Sinks.many().unicast().onBackpressureBuffer();
        var flux = sink.asFlux();
        sink.tryEmitNext("Hi");
        sink.tryEmitNext("How are you");
        sink.tryEmitNext("?");
        sink.tryEmitComplete();
        StepVerifier.create(flux).expectNext("Hi", "How are you", "?").verifyComplete();
    }

    @Test
    public void sink_multicast_test01() {
        var sink = Sinks.many().multicast().onBackpressureBuffer();
        var flux = sink.asFlux();
        flux.subscribe(x -> log.info("sam: {}", x));
        flux.subscribe(x -> log.info("kevin: {}", x));
        sink.tryEmitNext("Hi");
        sink.tryEmitNext("How are you");
        sink.tryEmitNext("?");

        flux.subscribe(x -> log.info("jake: {}", x));

        sink.tryEmitNext("jake");
        sink.tryEmitComplete();
    }

    @Test
    public void sink_multicast_test02() {
        var sink = Sinks.many().multicast().onBackpressureBuffer();
        var flux = sink.asFlux();
        
        sink.tryEmitNext("Hi");
        sink.tryEmitNext("How are you");
        sink.tryEmitNext("?");

        flux.subscribe(x -> log.info("sam: {}", x)); // 訊息會被放置 buffer，因此只有 sam 能夠有 buffer 資源
        flux.subscribe(x -> log.info("kevin: {}", x));
        flux.subscribe(x -> log.info("jake: {}", x));

        sink.tryEmitNext("jake");
        sink.tryEmitComplete();
    }

    @Test
    public void sink_replay_test01() {
        var sink = Sinks.many().replay().all();
        var flux = sink.asFlux();
        
        
        flux.subscribe(x -> log.info("sam: {}", x)); 
        flux.subscribe(x -> log.info("kevin: {}", x));
        sink.tryEmitNext("Hi");
        sink.tryEmitNext("How are you");
        sink.tryEmitNext("?");
        
        flux.subscribe(x -> log.info("jake: {}", x));

        sink.tryEmitNext("jake");
        sink.tryEmitComplete();
    }
}
