package org.reactor.sec10;


import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.test.StepVerifier;

public class BatchWindowsGroupTest {
    private static final Logger log = LoggerFactory.getLogger(BatchWindowsGroupTest.class);

    private static Flux<String> eventStream() {
        return Flux.interval(Duration.ofMillis(200)).map(x -> "event-" + (x + 1));
    }

    @Test
    public void buffer_test01() {
        // Flux<List<String>> buffer = eventStream().buffer(); int-max value or the
        // source has to complete
        Flux<List<String>> buffer = eventStream().take(6).buffer(3).doOnNext(x -> log.debug("{}", x)); // every 3 item
        StepVerifier.create(buffer)
                .expectNext(List.of("event-1", "event-2", "event-3")) // 第一組緩存
                .expectNext(List.of("event-4", "event-5", "event-6")) // 第二組緩存
                .verifyComplete();

    }

    @Test
    public void buffer_test02() throws InterruptedException {
        eventStream()
                .buffer(Duration.ofSeconds(1))
                .doOnNext(x -> log.debug("{}", x))
                .subscribe(x -> log.debug("r: {}", x));
        ;
        Thread.sleep(3000);
    }

    @Test
    public void bufferTimeout_test02() throws InterruptedException {
        eventStream()
                .bufferTimeout(5, Duration.ofSeconds(1))
                .doOnNext(x -> log.debug("{}", x))
                .subscribe(x -> log.debug("r: {}", x));
        ;
        Thread.sleep(5000);
    }

    /**
     * Buffer 需要有等待。回傳: Flux<List<T>>
     * 需要一次性收集 n 個元素並處理，例如 批量存儲到資料庫。
     * 需要保留數據整體性，例如 List<T> 處理。
     * Windows 不須等待，將元素往訂閱者丟。回傳:Flux<Flux<T>>
     * 逐步流式處理 n 個元素，例如 逐個傳輸 或 逐個處理異步請求。
     * 不想一次性加載 List<T>，而是 按需處理。
     */
    @Test
    public void windows_test01() throws InterruptedException {
        eventStream()
                .window(5)
                .doOnNext(x -> log.debug("{}", x))
                .flatMap(fl -> fl)
                .subscribe(x -> log.debug("r: {}", x));
        ;
        Thread.sleep(5000);
    }

    @Test
    public void groupby_test() {
        Flux<Product> products = Flux.just(
                new Product("Laptop", "Electronics"),
                new Product("Phone", "Electronics"),
                new Product("Sofa", "Furniture"),
                new Product("Table", "Furniture"),
                new Product("Headphones", "Electronics"),
                new Product("Chair", "Furniture"));

        Flux<GroupedFlux<String, Product>> groupedFlux = products
                .groupBy(Product::category);

        groupedFlux.flatMap(group -> group.collectList()
                .map(list -> "Category [" + group.key() + "]: " + list))
                .subscribe(x -> log.debug("{}", x));
    }

    record Product(String name, String category) {
        @Override
        public String toString() {
            return name;
        }
    }
}
