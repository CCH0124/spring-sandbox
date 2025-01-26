package org.reactor.sec08;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class BackPressureTest {
    private static final Logger log = LoggerFactory.getLogger(BackPressureTest.class);

    @Test
    public void automatic_backpressure() throws InterruptedException {
        
        // System.setProperty("reactor.bufferSize.small", "16");
        /**
         * default buffer is 256
         */
        var producer = Flux.generate(() -> 1, (state, sink) -> {
            log.info("generating {}", state);
            sink.next(state);
            return ++state;
        }).cast(Integer.class)
                .subscribeOn(Schedulers.parallel());

        producer.publishOn(Schedulers.boundedElastic())
        .map(BackPressureTest::timeConsumingTask)
        .subscribe(x -> log.info("received: {}", x));

        Thread.sleep(Duration.ofSeconds(60));
    }

    private static Integer timeConsumingTask(int i) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return i;
    }

    @Test
    public void automatic_backpressure_02() throws InterruptedException {
        
        
        /**
         * Setting buffer to 16
         * 75% Queue 會被檢查
         */
        System.setProperty("reactor.bufferSize.small", "16");
        var producer = Flux.generate(() -> 1, (state, sink) -> {
            log.info("generating {}", state);
            sink.next(state);
            return ++state;
        }).cast(Integer.class)
                .subscribeOn(Schedulers.parallel());

        producer.publishOn(Schedulers.boundedElastic())
        .map(BackPressureTest::timeConsumingTask)
        .subscribe(x -> log.info("received: {}", x));

        Thread.sleep(Duration.ofSeconds(60));
    }

    @Test
    public void limitRate_backpressure() throws InterruptedException {
        
        var producer = Flux.generate(() -> 1, (state, sink) -> {
            log.info("generating {}", state);
            sink.next(state);
            return ++state;
        }).cast(Integer.class)
                .subscribeOn(Schedulers.parallel());

        producer
        .limitRate(5)
        .publishOn(Schedulers.boundedElastic())
        .map(BackPressureTest::timeConsumingTask)
        .subscribe(x -> log.info("received: {}", x));

        Thread.sleep(Duration.ofSeconds(60));
    }

    @Test
    public void buffer_backpressure() throws InterruptedException {
        
        var producer = Flux.create( sink -> {
            for (int i = 0; i < 100 && !sink.isCancelled(); i++) {
                log.info("generating : {}", i);
                sink.next(i);
                try {
                    Thread.sleep(Duration.ofMillis(50));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).cast(Integer.class)
                .subscribeOn(Schedulers.parallel());

        producer
        .onBackpressureBuffer()
        .limitRate(5)
        .publishOn(Schedulers.boundedElastic())
        .map(BackPressureTest::timeConsumingTask)
        .subscribe(x -> log.info("received: {}", x));

        Thread.sleep(Duration.ofSeconds(60));
    }

    @Test
    public void error_backpressure() throws InterruptedException {
        
        var producer = Flux.create( sink -> {
            for (int i = 0; i < 100 && !sink.isCancelled(); i++) {
                log.info("generating : {}", i);
                sink.next(i);
                try {
                    Thread.sleep(Duration.ofMillis(50));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).cast(Integer.class)
                .subscribeOn(Schedulers.parallel());

        producer
        .onBackpressureError()
        .limitRate(5)
        .publishOn(Schedulers.boundedElastic())
        .map(BackPressureTest::timeConsumingTask)
        .subscribe(x -> log.info("received: {}", x));

        Thread.sleep(Duration.ofSeconds(60));
        /**
         *  [boundedElastic-1] ERROR reactor.core.publisher.Operators - Operator called default onErrorDropped
            reactor.core.Exceptions$ErrorCallbackNotImplemented: reactor.core.Exceptions$OverflowException: The receiver is overrun by more signals than expected (bounded queue...)
            Caused by: reactor.core.Exceptions$OverflowException: The receiver is overrun by more signals than expected (bounded queue...)
         */
    }

    @Test
    public void buffer_error_backpressure() throws InterruptedException {
        
        var producer = Flux.create( sink -> {
            for (int i = 0; i < 100 && !sink.isCancelled(); i++) {
                log.info("generating : {}", i);
                sink.next(i);
                try {
                    Thread.sleep(Duration.ofMillis(50));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).cast(Integer.class)
                .subscribeOn(Schedulers.parallel());

        producer
        .onBackpressureBuffer(10) // onBackpressureBuffer + onBackpressureError
        .limitRate(5)
        .publishOn(Schedulers.boundedElastic())
        .map(BackPressureTest::timeConsumingTask)
        .subscribe(x -> log.info("received: {}", x));

        Thread.sleep(Duration.ofSeconds(60));
        /**
         * [boundedElastic-1] ERROR reactor.core.publisher.Operators - Operator called default onErrorDropped
            reactor.core.Exceptions$ErrorCallbackNotImplemented: reactor.core.Exceptions$OverflowException: The receiver is overrun by more signals than expected (bounded queue...)
            Caused by: reactor.core.Exceptions$OverflowException: The receiver is overrun by more signals than expected (bounded queue...)
         */
    }

    @Test
    public void drop_backpressure() throws InterruptedException {
        
        var producer = Flux.create( sink -> {
            for (int i = 0; i < 100 && !sink.isCancelled(); i++) {
                log.info("generating : {}", i);
                sink.next(i);
                try {
                    Thread.sleep(Duration.ofMillis(50));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).cast(Integer.class)
                .subscribeOn(Schedulers.parallel());

        producer
        .onBackpressureDrop()
        .log() // request
        .limitRate(5) // next
        .publishOn(Schedulers.boundedElastic())
        .map(BackPressureTest::timeConsumingTask)
        .subscribe(x -> log.info("received: {}", x));

        Thread.sleep(Duration.ofSeconds(60));
    }

    @Test
    public void latest_backpressure() throws InterruptedException {
        
        var producer = Flux.create( sink -> {
            for (int i = 0; i < 100 && !sink.isCancelled(); i++) {
                log.info("generating : {}", i);
                sink.next(i);
                try {
                    Thread.sleep(Duration.ofMillis(50));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).cast(Integer.class)
                .subscribeOn(Schedulers.parallel());

        producer
        .onBackpressureLatest()
        .log() // request
        .limitRate(5) // next
        .publishOn(Schedulers.boundedElastic())
        .map(BackPressureTest::timeConsumingTask)
        .subscribe(x -> log.info("received: {}", x));

        Thread.sleep(Duration.ofSeconds(60));
    }
}
