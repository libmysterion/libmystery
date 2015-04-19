package com.mystery.libmystery.event;

import java.util.ArrayList;
import java.util.List;

class HandlerList<T> {

    private final List<Handler<T>> handlers = new ArrayList<>();

    void put(Handler<T> handler) {
        synchronized (handlers) {
            this.handlers.add(handler);
        }
    }

    void remove(Handler<T> handler) {
        synchronized (handlers) {
            handlers.removeIf((t) -> t == handler);
        }
    }

    void handle(T msg) {
        synchronized (handlers) {
            handlers.removeIf((t) -> t instanceof WeakHandler && !((WeakHandler) t).isRetained());
        }

        // needs to sync or copy...prefer copy
        handlers.forEach((t) -> {
            t.handle(msg);
        });
    }

    boolean isEmpty() {
        synchronized (handlers) {
            return this.handlers.isEmpty();
        }
    }

}
