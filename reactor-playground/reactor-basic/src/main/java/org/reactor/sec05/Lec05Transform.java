package org.reactor.sec05;

import java.util.function.UnaryOperator;


import reactor.core.publisher.Flux;

public class Lec05Transform {
    
    public static UnaryOperator<Flux<String>> uppercase() {
        return f -> {
            return f
            .map(x -> String.valueOf(x))
            .map(String::toUpperCase);
        };
    }
}
