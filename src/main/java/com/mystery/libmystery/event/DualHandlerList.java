package com.mystery.libmystery.event;

import java.util.ArrayList;
import java.util.List;

public class DualHandlerList<T, X> {
    
    
    
    private final List<DualHandler<T, X>> handlers = new ArrayList<>();

    void put(DualHandler<T, X> handler) {
        synchronized (handlers) {
            this.handlers.add(handler);
        }
    }

    void remove(DualHandler<T, X> handler) {
        synchronized (handlers) {
            handlers.removeIf((t) -> t == handler);
        }
    }

    void handle(T msg, X xtra) {
        synchronized (handlers) {
            handlers.removeIf((t) -> t instanceof WeakDualHandler && !((WeakDualHandler) t).isRetained());
        }
        
        // needs to make a copy as it could be modified during iteration
        handlers.forEach((t) -> {
            t.handle(msg, xtra);
        });
    }

    boolean isEmpty() {
        synchronized(handlers){
            return this.handlers.isEmpty();
        }
    }
}
