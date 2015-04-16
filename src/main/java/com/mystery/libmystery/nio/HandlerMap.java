package com.mystery.libmystery.nio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

class HandlerMap {

    private final List<MapItem<? extends Serializable>> map = new ArrayList<>();

    private class MapItem<T> {

        private Class<T> clazz;
        private Handler<T> handler;

        MapItem(Class<T> clazz, Handler<T> handler) {
            this.clazz = clazz;
            this.handler = handler;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.clazz);
            hash = 97 * hash + Objects.hashCode(this.handler);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MapItem<?> other = (MapItem<?>) obj;
            if (!Objects.equals(this.clazz, other.clazz)) {
                return false;
            }
            if (!Objects.equals(this.handler, other.handler)) {
                return false;
            }
            return true;
        }

    }

    <H extends Serializable> void put(Class<H> clazz, Handler<H> handler) {
        synchronized (map) {
            this.map.add(new MapItem<>(clazz, handler));
        }
    }

    <H extends Serializable> void remove(Handler<H> handler) {
        synchronized (map) {
            Optional<MapItem<? extends Serializable>> findAny = map.stream()
                    .filter((t) -> t.handler == handler)
                    .findAny();
            if (findAny.isPresent()) {
                map.remove(findAny.get());
            }
        }
    }

    <M extends Serializable> void handle(M msg) {
        List<Handler<M>> handlers;

        synchronized (map) {
            List<MapItem> collectedHandlers = map.stream()
                    .filter((t) -> t.handler instanceof WeakHandler)
                    .filter((w) -> !((WeakHandler) w.handler).isRetained())
                    .collect(Collectors.toList());
            map.removeAll(collectedHandlers);

            handlers = map.stream()
                    .filter((t) -> t.clazz == msg.getClass())
                    .map((t) -> (Handler<M>) t.handler)
                    .collect(Collectors.toList());
        }

        handlers.forEach((t) -> t.handle(msg));
    }

}
