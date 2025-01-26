package org.reactor.sec09;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class CombiningPublisherTest {
    private static final Logger log = LoggerFactory.getLogger(CombiningPublisherTest.class);

    @Test
    public void startWith_test01() {
        var producer1 = Flux.just(1, 2, 3)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .delayElements(Duration.ofMillis(10));

        var sub = producer1
                .startWith(-1, 0);

        StepVerifier.create(sub).expectNext(-1, 0, 1, 2, 3).verifyComplete();
    }

    @Test
    public void startWith_test02() {
        var producer1 = Flux.just(1, 2, 3)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .delayElements(Duration.ofMillis(10));

        var sub = producer1
                .startWith(List.of(-2, -1, 0));

        StepVerifier.create(sub).expectNext(-2, -1, 0, 1, 2, 3).verifyComplete();
    }

    @Test
    public void startWith_test03() {
        var producer1 = Flux.just(1, 2, 3)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .delayElements(Duration.ofMillis(10));

        var producer2 = Flux.just(61, 62, 63)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .delayElements(Duration.ofMillis(10));

        var sub = producer1
                .startWith(producer2);

        StepVerifier.create(sub.take(3)).expectNext(61, 62, 63).verifyComplete();
    }

    @Test
    public void startWith_test04() {
        var producer1 = Flux.just(1, 2, 3)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .delayElements(Duration.ofMillis(10));

        var producer2 = Flux.just(61, 62, 63)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .delayElements(Duration.ofMillis(10));

        var sub = producer1
                .startWith(producer2)
                .startWith(1000);

        StepVerifier.create(sub.take(3)).expectNext(1000, 61, 62).verifyComplete();
    }

    @Test
    public void concatValue_test() {
        var producer1 = Flux.just(1, 2, 3)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .delayElements(Duration.ofMillis(10));

        var sub = producer1.concatWithValues(-1, 0);

        StepVerifier.create(sub).expectNext(1, 2, 3, -1, 0).verifyComplete();
    }

    @Test
    public void concatWith_test() {
        var producer1 = Flux.just(1, 2, 3)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .delayElements(Duration.ofMillis(10));

        var producer2 = Flux.just(61, 62, 63)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .delayElements(Duration.ofMillis(10));

        var sub = producer1.concatWith(producer2);

        StepVerifier.create(sub).expectNext(1, 2, 3, 61, 62, 63).verifyComplete();
    }

    @Test
    public void concat_test() {
        var producer1 = Flux.just(1, 2, 3)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .delayElements(Duration.ofMillis(10));

        var producer2 = Flux.just(61, 62, 63)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .delayElements(Duration.ofMillis(10));

        var sub = Flux.concat(producer1, producer2);

        StepVerifier.create(sub).expectNext(1, 2, 3, 61, 62, 63).verifyComplete();
    }

    @Test
    public void concatDelayError_test() {
        var producer1 = Flux.just(1, 2, 3)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .delayElements(Duration.ofMillis(10));

        var producer2 = Flux.just(61, 62, 63)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .delayElements(Duration.ofMillis(10));

        var produce3 = Flux.error(new RuntimeException("oops"));

        var sub = Flux.concatDelayError(producer1, produce3, producer2);

        StepVerifier.create(sub.take(6)).expectNext(1, 2, 3, 61, 62, 63).verifyComplete();
        StepVerifier.create(sub.last()).expectError(RuntimeException.class).verify();
    }

    @Test
    public void merge_test() throws InterruptedException {
        var producer1 = Flux.just(1, 2, 3)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .doOnCancel(() -> log.info("cancelling producer1"))
                .doOnComplete(() -> log.info("producer1 complete"))
                .delayElements(Duration.ofMillis(10));

        var producer2 = Flux.just(61, 62, 63)
                .doOnSubscribe(s -> log.info("subscribing to producer2"))
                .doOnCancel(() -> log.info("cancelling producer2"))
                .doOnComplete(() -> log.info("producer2 complete"))
                .delayElements(Duration.ofMillis(10));

        var producer3 = Flux.just(11, 12, 13)
                .doOnSubscribe(s -> log.info("subscribing to producer3"))
                .doOnCancel(() -> log.info("cancelling producer3"))
                .doOnComplete(() -> log.info("producer3 complete"))
                .delayElements(Duration.ofMillis(10));

        Flux.merge(producer1, producer2, producer3).subscribe(x -> log.debug("{}", x));

        Thread.sleep(3000);
    }

    @Test
    public void mergeWith_test() throws InterruptedException {
        var producer1 = Flux.just(1, 2, 3)
                .doOnSubscribe(s -> log.info("subscribing to producer1"))
                .doOnCancel(() -> log.info("cancelling producer1"))
                .doOnComplete(() -> log.info("producer1 complete"))
                .delayElements(Duration.ofMillis(10));

        var producer2 = Flux.just(61, 62, 63)
                .doOnSubscribe(s -> log.info("subscribing to producer2"))
                .doOnCancel(() -> log.info("cancelling producer2"))
                .doOnComplete(() -> log.info("producer2 complete"))
                .delayElements(Duration.ofMillis(10));

        var producer3 = Flux.just(11, 12, 13)
                .doOnSubscribe(s -> log.info("subscribing to producer3"))
                .doOnCancel(() -> log.info("cancelling producer3"))
                .doOnComplete(() -> log.info("producer3 complete"))
                .delayElements(Duration.ofMillis(10));

        producer1
                .mergeWith(producer2)
                .mergeWith(producer3)
                .subscribe(x -> log.debug("{}", x));

        Thread.sleep(3000);
    }

    @Test
    public void zip_test() throws InterruptedException {
        record Transaction(String transactionId, double amount) {
        }

        record TransactionLog(String userId, String transactionId, double amount) {
        }

        Mono<User> userMono = Mono.just(new User("U123", "Alice"))
                .delayElement(Duration.ofMillis(500));
        Flux<Transaction> transactionFlux = Flux.just(
                new Transaction("T001", 100.0),
                new Transaction("T002", 200.0),
                new Transaction("T003", 300.0))
                .delayElements(Duration.ofMillis(300));
        var combinedFlux = Flux.zip(userMono.repeat(transactionFlux.count().block().intValue()), transactionFlux)
                .map(t -> new TransactionLog(t.getT1().userId(), t.getT2().transactionId(), t.getT2().amount()));

        combinedFlux.subscribe(x -> log.info("{}", x));
        Thread.sleep(3000);
    }

    @Test
    public void flatMap_test() throws InterruptedException {
        /**
         * flatMap 無序
         * concatMap 有序
         */
        Flux<User> users = Flux.just(
                new User("U123", "Alice"),
                new User("U124", "Bob"),
                new User("U125", "Charlie"));
        users.flatMap(user -> getOrdersForUser(user.userId())
                .map(order -> "User: " + user.name() +
                        " placed an order for: " + order.item()))
                .subscribe(x -> log.info("{}", x));
        Thread.sleep(3000);
    }

    Flux<Order> getOrdersForUser(String userId) {
        return switch (userId) {
            case "U123" -> Flux.just(
                    new Order("O101", "Laptop"),
                    new Order("O102", "Mouse")).delayElements(Duration.ofMillis(300));
            case "U124" -> Flux.just(
                    new Order("O103", "Keyboard")).delayElements(Duration.ofMillis(300));
            case "U125" -> Flux.just(
                    new Order("O104", "Monitor"),
                    new Order("O105", "Desk"),
                    new Order("O106", "Chair")).delayElements(Duration.ofMillis(300));
            default -> Flux.empty();
        };
    }

    @Test
    public void collectList_test() {
        Mono<List<Integer>> collectList = Flux.range(1, 10)
                .collectList();

        // Act & Assert: 使用 StepVerifier 驗證行為
        StepVerifier.create(collectList)
                .expectNextMatches(list -> {
                    assertEquals(10, list.size());
                    assertIterableEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), list);
                    return true; // 讓 StepVerifier 繼續驗證
                })
                .verifyComplete();

        Mono<List<Integer>> error = Flux.range(1, 10)
                .concatWith(Mono.error(new RuntimeException("oops")))
                .collectList();
        StepVerifier.create(error).expectError(RuntimeException.class).verify();

    }

}

record User(String userId, String name) {
}

record Order(String orderId, String item) {
}