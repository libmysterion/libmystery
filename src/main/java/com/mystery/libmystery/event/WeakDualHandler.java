package com.mystery.libmystery.event;

import com.mystery.libmystery.event.DualHandler;
import java.lang.ref.WeakReference;

public class WeakDualHandler<A, B> implements DualHandler<A, B>{

    private WeakReference<DualHandler<A, B>> ref;

    public WeakDualHandler(DualHandler<A, B> handler) {
        ref = new WeakReference<>(handler);
    }

    @Override
    public void handle(A a, B b) {
        DualHandler<A, B> get = ref.get();
        if (get != null) {
            get.handle(a, b);
        }
    }

    boolean isRetained() {
        return ref.get() != null;
    }

}