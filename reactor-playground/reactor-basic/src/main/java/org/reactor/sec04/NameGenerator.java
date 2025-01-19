package org.reactor.sec04;

import java.util.function.Consumer;

import org.reactor.common.Utils;

import reactor.core.publisher.FluxSink;

public class NameGenerator implements Consumer<FluxSink<String>> {

    private FluxSink<String> sink;

    @Override
    public void accept(FluxSink<String> t) {
        this.sink = t;
    }

    public void generate() {
        this.sink.next(Utils.instance().coffee().name1());
    }
    
}
