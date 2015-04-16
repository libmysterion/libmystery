package com.mystery.libmystery.nio;

import java.lang.ref.WeakReference;


public class WeakHandler<T> implements Handler<T>{

    private WeakReference<Handler<T>> ref;

    public WeakHandler(Handler<T> handler) {
        ref = new WeakReference<>(handler);
    }

    @Override
    public void handle(T t) {
        Handler<T> get = ref.get();
        if (get != null) {
            get.handle(t);
        }
    }

    boolean isRetained() {
        return ref.get() != null;
    }

}
