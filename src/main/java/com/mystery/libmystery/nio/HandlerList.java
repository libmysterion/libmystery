package com.mystery.libmystery.nio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class HandlerList<T> {

    private final List<Handler<T>> handlers = new ArrayList<>();

    void put(Handler<T> handler) {
        synchronized (handlers) {
            this.handlers.add(handler);
        }
    }

    void remove(Handler<T> handler) {
        synchronized (handlers) {
            Optional<Handler<T>> findAny = handlers.stream()
                    .filter((t) -> t == handler)
                    .findAny();
            if (findAny.isPresent()) {
                handlers.remove(findAny.get());
            }
        }
    }

    void handle(T msg) {
        synchronized (handlers) {
            List<Handler<T>> collectedHandlers = handlers.stream()
                    .filter((t) -> t instanceof WeakHandler)
                    .filter((w) -> !((WeakHandler) w).isRetained())
                    .collect(Collectors.toList());
            handlers.removeAll(collectedHandlers);
        }
        
        handlers.forEach((t) -> {
            t.handle(msg);
        });
    }

}
