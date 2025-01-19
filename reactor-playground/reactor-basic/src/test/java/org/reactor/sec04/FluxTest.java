package org.reactor.sec04;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.reactor.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxTest {

    private static final Logger log = LoggerFactory.getLogger(FluxTest.class);

    @Test
    public void slink_test() {
        var nameGenerator = new NameGenerator();
        var flux = Flux.create(nameGenerator);
        flux.subscribe(x -> log.info("company {}", x));

        IntStream.range(1, 10).forEach(x -> nameGenerator.generate());
    }

    @Test
    public void take_test() {
        Flux.range(1, 10)
                .log("take")
                .take(3) // 完成時會進行 cancel 接著 onComplete
                .log("sub")
                .subscribe(i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));

    }

    @Test
    public void takeWhile_test() {
        Flux.range(1, 10)
                .log("take")
                .takeWhile(i -> i < 5) // cancel() stop when the condition is not met, like while loop
                .log("sub") // onComplete()
                .subscribe(i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));

    }

    @Test
    public void takeUntil_test() {
        var flux = Flux.range(1, 10)
                .log("take")
                .takeUntil(i -> i < 5) // cancel() stop when the condition is met. and allow last item.
                .log("sub"); // onComplete()

        StepVerifier.create(flux)
                .expectNext(1)
                .verifyComplete();

        flux.subscribe(i -> log.info("received: {}", i),
                err -> log.error("error: {}", err),
                () -> log.info("completed."));

    }

    @Test
    public void take_example() {
        Flux.range(1, 100)
                .take(25) // 1 ... 25
                .takeWhile(i -> i < 10) // 1, 2, 3, 4, 5, 6, 7, 8, 9
                .takeUntil(i -> i > 1 && i < 5) // 2 滿足條件停止
                .take(3)
                .subscribe(i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));
    }

    /*
     * - invoke the given lambda expression again base on downstream demand
     * - We can emit only one value at a time
     * Will stop when complete method is invoked
     * Will stop when error method is invoked
     * will stop downstrean cancels
     */
    @Test
    public void generate_test() {
        var set = new HashSet<>();
        Flux.generate(synchronousSink -> {
            log.info("invoked");
            synchronousSink.next(1);
            synchronousSink.complete();
        }).subscribe(i -> set.add(i),
                err -> log.error("error: {}", err),
                () -> log.info("completed."));

        assertEquals(1, set.size());

        Flux<Integer> flux = Flux.generate(synchronousSink -> {
            log.info("invoked");
            synchronousSink.next(1);
            synchronousSink.error(new IllegalArgumentException("oops"));
        });
        StepVerifier.create(flux)
                .expectNext(1)
                .expectError(IllegalArgumentException.class).verify();
    }

    @Test
    public void generate_with_takeUntil_test() {
        var set = new HashSet<>();
        Flux.<String>generate(synchronousSink -> {
            log.info("invoked");
            var fruitNamer = Utils.instance().food().fruit();
            set.add(fruitNamer);
            synchronousSink.next(fruitNamer);
            synchronousSink.next(fruitNamer);
        })
                .takeUntil(x -> set.size() > 5)
                .subscribe(i -> log.info("received: {}", i),
                        err -> log.error("error: {}", err),
                        () -> log.info("completed."));
        ;

        assertEquals(6, set.size());
    }

    @Test
    public void generate_with_state_test() {
        var characterFlux = Flux.generate(
                () -> 97, // Callable function. Defines the initial state. invoked once
                (state, sink) -> {
                    char value = (char) state.intValue();
                    sink.next(value);
                    if (value == 'z') {
                        sink.complete();
                    }
                    return state + 1;
                });

        StepVerifier.create(characterFlux.take(3))
                .expectNext('a', 'b', 'c')
                .expectComplete()
                .verify();
    }
}
