package org.reactor.sec03;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.reactor.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class JustTest {
        private static final Logger log = LoggerFactory.getLogger(JustTest.class);

        @Test
        public void just_test() {
                Flux.just(1, 2, 3, "itachi")
                                .subscribe(x -> log.info("received {}", x));
        }

        @Test
        public void collection_test() {
                var list = List.of("a", "b", "c");
                Flux.fromIterable(list)
                                .subscribe(x -> log.info("received {}", x));

                var set = Set.of(1, 2, 3, 4);
                Flux.fromIterable(set)
                                .subscribe(x -> log.info("received {}", x));

                Integer[] arr = { 1, 3, 5, 7 };

                Flux.fromArray(arr)
                                .subscribe(x -> log.info("received {}", x));

        }

        @Test
        public void stream_test() {
                var list = List.of(1, 2, 3, 4);
                // 只能被一個訂閱，除非使用 supply
                Stream<Integer> stream = list.stream();

                Flux.fromStream(stream)
                                .subscribe(x -> log.info("received {}", x));

                Flux.fromStream(list::stream)
                                .subscribe(x -> log.info("received {}", x));

                Flux.fromStream(list::stream)
                                .subscribe(x -> log.info("received {}", x));

        }

        @Test
        public void range_test() {
                Flux.range(1, 10)
                                .map(i -> Utils.instance().company().logo())
                                .subscribe(x -> log.info("received {}", x));
        }

        @Test
        public void log_test() {
                Flux.range(1, 10)
                                .log()
                                .map(i -> Utils.instance().company().logo())
                                .log("map-sub")
                                .subscribe(x -> log.info("received {}", x));
        }

        @Test
        public void interval_test() {
                Flux.interval(Duration.ZERO, Duration.ofSeconds(2))
                                .map(i -> Utils.instance().food().dish())
                                .take(5)
                                .subscribe(x -> log.info("received {}", x));

        }

        @Test
        public void empty_error_test() {
                Flux.empty()
                .doOnComplete(() -> log.info("complete empty."))
                .subscribe(x -> log.info("received {}", x));

                Flux.error(new IllegalArgumentException("oops"))
                .subscribe(x -> log.error("received {}", x));
        }

        @Test
        public void mono_to_flux_test() {
                Mono<Integer> just = Mono.just(1);
                Flux.from(just)
                .next()
                .subscribe(x -> log.error("received {}", x));
        }
}
