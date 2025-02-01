package org.reactor.sec11;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.reactor.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

public class RepeatAndRetryTest {
    private static final Logger log = LoggerFactory.getLogger(RepeatAndRetryTest.class);

    private static Mono<String> getCountryName() {
        return Mono.fromSupplier(() -> Utils.instance().country().name());
    }

    private static Mono<String> getCountryName02() {
        var ai = new AtomicInteger(0);
        return Mono.fromSupplier(() -> {
            if (ai.incrementAndGet() < 3) {
                throw new IllegalArgumentException("oops");
            }
            return Utils.instance().country().name();
        })
                .doOnError(err -> log.error("{}", err.getMessage()))
                .doOnSubscribe(s -> log.info("subscribing"));
    }

    @Test
    public void repeat_test01() {
        Flux<String> repeat01 = getCountryName().repeat(3);
        repeat01.subscribe(x -> log.debug("country {}", x));
        StepVerifier.create(repeat01).expectNextCount(4).verifyComplete();
    }

    @Test
    public void repeat_test02() {
        Flux<String> repeat = getCountryName().repeat();
        repeat.take(5).subscribe(x -> log.debug("country {}", x));
        StepVerifier.create(repeat.take(4)).expectNextCount(4).verifyComplete();
    }

    @Test
    public void repeat_test03() throws InterruptedException {
        Flux<String> repeat = getCountryName().repeatWhen(f -> f.delayElements(Duration.ofSeconds(2)).take(5));
        repeat.subscribe(x -> log.debug("country {}", x));
        StepVerifier.create(repeat).expectNextCount(5);
        Thread.sleep(15000);
    }

    @Test
    public void repeat_test04() {
        /*
         * repeat(1)：原始 Flux 正常完成後，再執行 1 次（總共 2 次）。
           repeat(2)：再執行 2 次，累計 4 次（不是 3 次，因為它是基於前面的 repeat(1)）。
         */
        Flux.just("a")
                .repeat(1)
                .repeat(2)
                .subscribe(
                        i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));

    }

    @Test
    public void retry_test01() {
        Mono<String> retry = getCountryName02().retry(2);
        retry.subscribe(
                i -> log.info("received: {}", i),
                err -> log.error("error: {}", err),
                () -> log.info("completed."));
    }

    @Test
    public void retry_test02() {
        /*
         * retry(1)：如果 Flux 發生錯誤，它會最多重新訂閱 1 次。
           retry(2)：雖然 retry(2) 會再次指定 最多重新訂閱 2 次，但 上面 retry(1) 會優先處理錯誤，所以第二個 retry(2) 其實沒有影響。
         */
        Flux.just("a")
                .retry(1)
                .retry(2)
                .subscribe(
                        i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));
    }

    @Test
    public void retryWhen_test01() {
        Mono<String> retry = getCountryName02().retryWhen(Retry.max(2));
        retry.subscribe(
                i -> log.info("received: {}", i),
                err -> log.error("error: {}", err),
                () -> log.info("completed."));
    }

    @Test
    public void retryWhen_test02() throws InterruptedException {
        Mono<String> retry = getCountryName02().retryWhen(
                Retry.fixedDelay(2,
                        Duration.ofSeconds(1)).doBeforeRetry(x -> log.warn("retrying")));
        retry.subscribe(
                i -> log.info("received: {}", i),
                err -> log.error("error: {}", err),
                () -> log.info("completed."));

        Thread.sleep(4000);
    }
}
