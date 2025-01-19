package org.reactor.sec05;

import java.time.Duration;
import java.util.Objects;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.reactor.common.Utils;
import org.reactor.sec04.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class OperatorTest {

    private static final Logger log = LoggerFactory.getLogger(OperatorTest.class);

    @Test
    public void handle_test() {
        Flux
                .range(1, 10)
                .handle((item, synchronousSink) -> {
                    switch (item) {
                        case 1 -> synchronousSink.next(Utils.instance().aws().accountId());
                        case 3 -> synchronousSink.next(Utils.instance().azure().subscriptionId());
                        case 5 -> synchronousSink.error(new IllegalArgumentException("Not suppot."));
                        default -> synchronousSink.next(item);
                    }
                }).subscribe(i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));

    }

    @Test
    public void handle_02_test() {
        Flux
                .generate(synchronousSink -> synchronousSink.next(Utils.instance().country().name()))
                .handle((item, synchronousSink) -> {
                    synchronousSink.next(item);
                    if (item.equals("Canada")) {
                        synchronousSink.complete();
                    }
                }).subscribe(i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));
    }

    @Test
    public void do_Callback_test() {
        Flux.<Integer>create(fluxSink -> {
            log.info("producer begins");
            fluxSink.complete();
            log.info("producer ends");
        })
                .doOnComplete(() -> log.info("doOnComplete-1"))
                .doFirst(() -> log.info("doFirst-1"))
                .doOnNext(item -> log.info("doOnNext-1: {}", item))
                .doOnSubscribe(subscription -> log.info("doOnSubscribe-1: {}", subscription))
                .doOnRequest(request -> log.info("doOnRequest-1: {}", request))
                .doOnError(error -> log.info("doOnError-1: {}", error.getMessage()))
                .doOnTerminate(() -> log.info("doOnTerminate-1")) // complete or error case
                .doOnCancel(() -> log.info("doOnCancel-1"))
                .doOnDiscard(Object.class, o -> log.info("doOnDiscard-1: {}", o))
                .doFinally(signal -> log.info("doFinally-1: {}", signal)) // finally irrespective of the reason
                .doOnComplete(() -> log.info("doOnComplete-2"))
                .doFirst(() -> log.info("doFirst-2"))
                .doOnNext(item -> log.info("doOnNext-2: {}", item))
                .doOnSubscribe(subscription -> log.info("doOnSubscribe-2: {}", subscription))
                .doOnRequest(request -> log.info("doOnRequest-2: {}", request))
                .doOnError(error -> log.info("doOnError-2: {}", error.getMessage()))
                .doOnTerminate(() -> log.info("doOnTerminate-2")) // complete or error case
                .doOnCancel(() -> log.info("doOnCancel-2"))
                .doOnDiscard(Object.class, o -> log.info("doOnDiscard-2: {}", o))
                .doFinally(signal -> log.info("doFinally-2: {}", signal)) // finally irrespective of the reason
                .subscribe(i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));

        /**
         * org.reactor.sec05.OperatorTest - doFirst-2
         * org.reactor.sec05.OperatorTest - doFirst-1
         * org.reactor.sec05.OperatorTest - doOnSubscribe-1:
         * reactor.core.publisher.FluxPeekFuseable$PeekConditionalSubscriber@1ec9bd38
         * org.reactor.sec05.OperatorTest - doOnSubscribe-2:
         * reactor.core.publisher.FluxPeekFuseable$PeekConditionalSubscriber@3cfdd820
         * org.reactor.sec05.OperatorTest - doOnRequest-2: 9223372036854775807
         * org.reactor.sec05.OperatorTest - doOnRequest-1: 9223372036854775807
         * org.reactor.sec05.OperatorTest - producer begins
         * org.reactor.sec05.OperatorTest - doOnComplete-1
         * org.reactor.sec05.OperatorTest - doOnTerminate-1
         * org.reactor.sec05.OperatorTest - doOnComplete-2
         * org.reactor.sec05.OperatorTest - doOnTerminate-2
         * org.reactor.sec05.OperatorTest - completed.
         * org.reactor.sec05.OperatorTest - doFinally-2: onComplete
         * org.reactor.sec05.OperatorTest - doFinally-1: onComplete
         * org.reactor.sec05.OperatorTest - producer ends
         * 
         */
    }

    @Test
    public void do_Callback_error_test() {
        Flux.<Integer>create(fluxSink -> {
            log.info("producer begins");
            fluxSink.error(new IllegalArgumentException("oops"));
            log.info("producer ends");
        })
                .doOnComplete(() -> log.info("doOnComplete-1"))
                .doFirst(() -> log.info("doFirst-1"))
                .doOnNext(item -> log.info("doOnNext-1: {}", item))
                .doOnSubscribe(subscription -> log.info("doOnSubscribe-1: {}", subscription))
                .doOnRequest(request -> log.info("doOnRequest-1: {}", request))
                .doOnError(error -> log.info("doOnError-1: {}", error.getMessage()))
                .doOnTerminate(() -> log.info("doOnTerminate-1")) // complete or error case
                .doOnCancel(() -> log.info("doOnCancel-1"))
                .doOnDiscard(Object.class, o -> log.info("doOnDiscard-1: {}", o))
                .doFinally(signal -> log.info("doFinally-1: {}", signal)) // finally irrespective of the reason
                .doOnComplete(() -> log.info("doOnComplete-2"))
                .doFirst(() -> log.info("doFirst-2"))
                .doOnNext(item -> log.info("doOnNext-2: {}", item))
                .doOnSubscribe(subscription -> log.info("doOnSubscribe-2: {}", subscription))
                .doOnRequest(request -> log.info("doOnRequest-2: {}", request))
                .doOnError(error -> log.info("doOnError-2: {}", error.getMessage()))
                .doOnTerminate(() -> log.info("doOnTerminate-2")) // complete or error case
                .doOnCancel(() -> log.info("doOnCancel-2"))
                .doOnDiscard(Object.class, o -> log.info("doOnDiscard-2: {}", o))
                .doFinally(signal -> log.info("doFinally-2: {}", signal)) // finally irrespective of the reason
                .subscribe(i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));
    }

    @Test
    public void do_Callback_02_test() {
        Flux.<Integer>create(fluxSink -> {
            log.info("producer begins");
            for (int i = 0; i < 4; i++) {
                fluxSink.next(i);
            }
            fluxSink.complete();
            log.info("producer ends");
        })
                .doOnComplete(() -> log.info("doOnComplete-1"))
                .doFirst(() -> log.info("doFirst-1"))
                .doOnNext(item -> log.info("doOnNext-1: {}", item))
                .doOnSubscribe(subscription -> log.info("doOnSubscribe-1: {}", subscription))
                .doOnRequest(request -> log.info("doOnRequest-1: {}", request))
                .doOnError(error -> log.info("doOnError-1: {}", error.getMessage()))
                .doOnTerminate(() -> log.info("doOnTerminate-1")) // complete or error case
                .doOnCancel(() -> log.info("doOnCancel-1"))
                .doOnDiscard(Object.class, o -> log.info("doOnDiscard-1: {}", o))
                .doFinally(signal -> log.info("doFinally-1: {}", signal)) // finally irrespective of the reason
                .take(2)
                .doOnComplete(() -> log.info("doOnComplete-2"))
                .doFirst(() -> log.info("doFirst-2"))
                .doOnNext(item -> log.info("doOnNext-2: {}", item))
                .doOnSubscribe(subscription -> log.info("doOnSubscribe-2: {}", subscription))
                .doOnRequest(request -> log.info("doOnRequest-2: {}", request))
                .doOnError(error -> log.info("doOnError-2: {}", error.getMessage()))
                .doOnTerminate(() -> log.info("doOnTerminate-2")) // complete or error case
                .doOnCancel(() -> log.info("doOnCancel-2"))
                .doOnDiscard(Object.class, o -> log.info("doOnDiscard-2: {}", o))
                .doFinally(signal -> log.info("doFinally-2: {}", signal)) // finally irrespective of the reason
                .take(4)
                .subscribe(i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));
    }

    @Test
    public void delay_test() throws InterruptedException {
        /**
         * 使用 delay 時，每次都是 request(1)。非一次請求 request(unbounded)
         */
        Flux.range(1, 10)
                .log()
                .delayElements(Duration.ofSeconds(1))
                .subscribe(
                        i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));
        Thread.sleep(15000);
    }

    @Test
    public void error_handler_01_test() {
        var coffe = new NameGenerator();
        Flux.create(
                coffe)
                .map(x -> {
                    if (Objects.equals("Blue", x)) {
                        throw new IllegalArgumentException();
                    }
                    if (Objects.equals("The", x)) {
                        throw new ArithmeticException();
                    }
                    if (Objects.equals("Green", x)) {
                        throw new RuntimeException();
                    }
                    return x;

                })
                .onErrorReturn(IllegalArgumentException.class, "-1") // 錯誤時回傳對應值
                .onErrorReturn(ArithmeticException.class, "-2")
                .onErrorReturn("-3")
                .subscribe(
                        i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));
        IntStream.range(1, 10).forEach(x -> coffe.generate());
    }

    @Test
    public void error_handler_02_test() {
        var coffe = new NameGenerator();
        Flux.create(
                coffe)
                .map(x -> {
                    if (Objects.equals("Blue", x)) {
                        throw new IllegalArgumentException();
                    }
                    if (Objects.equals("The", x)) {
                        throw new ArithmeticException();
                    }
                    if (Objects.equals("Green", x)) {
                        throw new RuntimeException();
                    }
                    return x;

                })
                .onErrorResume(IllegalArgumentException.class, ex -> Flux.just("404"))
                .onErrorResume(ArithmeticException.class, ex -> Flux.error(new UnknownError()))
                .onErrorResume(ex -> Flux.just("403"))
                .onErrorReturn("-3") // 如果 onErrorResume 拋出錯誤則此 Operator 會抓
                .subscribe(
                        i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));
        IntStream.range(1, 10).forEach(x -> coffe.generate());
    }

    @Test
    public void error_handler_03_test() {
        var coffe = new NameGenerator();
        Flux.create(
                coffe)
                .map(x -> {
                    throw new IllegalArgumentException();
                })
                .onErrorComplete() // 當錯誤時視為處理完成
                .onErrorResume(IllegalArgumentException.class, ex -> Flux.just("404"))
                .onErrorReturn("-3") // 如果 onErrorResume 拋出錯誤則此 Operator 會抓
                .subscribe(
                        i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));
        IntStream.range(1, 10).forEach(x -> coffe.generate());
    }

    /**
     * 跳過錯誤繼續處理
     */
    @Test
    public void error_handler_04_test() {
        Flux.range(1, 10)
                .map(i -> i == 5 ? 5 / 0 : i)
                .onErrorContinue((ex, obj) -> log.error("error number {}", obj, ex))
                .subscribe(
                        i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));
    }

    @Test
    public void defaultEmpty_test() {
        var mono = Mono.just(10)
                .filter(x -> x / 3 == 0)
                .defaultIfEmpty(-1);

        StepVerifier.create(mono).expectNext(-1).verifyComplete();

        var mono1 = Mono.just(10)
                .filter(x -> x % 5 == 0)
                .defaultIfEmpty(-1);

        StepVerifier.create(mono1).expectNext(10).verifyComplete();
    }

    /**
     * Switch diff publisher.
     */

    @Test
    public void switchEmpty_test() {
        var mono = Mono.just(10)
                .filter(x -> x / 3 == 0)
                .switchIfEmpty(Mono.just(12));

        StepVerifier.create(mono).expectNext(12).verifyComplete();

        var mono1 = Mono.just(10)
                .filter(x -> x % 5 == 0)
                .switchIfEmpty(Mono.just(11));

        StepVerifier.create(mono1).expectNext(10).verifyComplete();
    }

    @Test
    public void timeout_test() throws InterruptedException {
        Mono.fromSupplier(() -> Utils.instance().commerce().brand())
                .delayElement(Duration.ofMillis(1900))
                .timeout(Duration.ofSeconds(1), Mono.fromSupplier(() -> "Faker-"+Utils.instance().commerce().brand()))
                .log()
                .subscribe(
                        i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));

        Thread.sleep(5000);
    }

    @Test
    public void transform_test() {
        Flux.range(1, 5)
        .map(i -> Utils.instance().commerce().productName())
        .transform(Lec05Transform.uppercase())
        .subscribe(
                        i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));
    }

}
