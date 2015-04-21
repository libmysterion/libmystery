package com.mystery.libmystery.event;

import com.mystery.libmystery.nio.AsynchronousObjectSocketChannel;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventEmitter {

        
    public static final Logger logger = LoggerFactory.getLogger(EventEmitter.class);
    
    private final HashMap<Object, HandlerList> handlers = new HashMap<>();
    private final HashMap<Object, DualHandlerList> dualHandlers = new HashMap<>();

    private <T> HandlerList<T> getHandlerList(Object key) {
        HandlerList handlerList = handlers.get(key);
        if (handlerList == null) {
            handlerList = new HandlerList<>();
            handlers.put(key, handlerList);
        }
        return handlerList;
    }

    private <T, X> DualHandlerList<T, X> getDualHandlerList(Object key) {
        DualHandlerList handlerList = dualHandlers.get(key);
        if (handlerList == null) {
            handlerList = new DualHandlerList<>();
            dualHandlers.put(key, handlerList);
        }
        return handlerList;
    }

    public <T> void off(String event, Handler<T> handler) {
        synchronized (handlers) {
            HandlerList<T> handlerList = getHandlerList(event);
            handlerList.remove(handler);
        }

    }

    public <T> void off(Class<T> event, Handler<T> handler) {
        synchronized (handlers) {
            HandlerList<T> handlerList = getHandlerList(event);
            handlerList.remove(handler);

        }

    }

    public <T> void off(Object event, Handler<T> handler) {
        synchronized (handlers) {
            HandlerList<T> handlerList = getHandlerList(event);
            handlerList.remove(handler);
        }
    }

    public <T> void on(String event, Handler<T> handler) {
        synchronized (handlers) {
            HandlerList<T> handlerList = getHandlerList(event);
            handlerList.put(handler);
        }

    }

    public <T> void on(Object event, Handler<T> handler) {
        synchronized (handlers) {
            HandlerList<T> handlerList = getHandlerList(event);
            handlerList.put(handler);
        }
    }

    public <T> void on(Class<T> event, Handler<T> handler) {
        synchronized (handlers) {

            HandlerList<T> handlerList = getHandlerList(event);
            handlerList.put(handler);
        }
    }

    public <T, X> void on(String event, DualHandler<T, X> handler) {
        synchronized (dualHandlers) {
            DualHandlerList<T, X> handlerList = getDualHandlerList(event);
            handlerList.put(handler);
        }
    }

    public <T, X> void on(Object event, DualHandler<T, X> handler) {
        synchronized (dualHandlers) {
            DualHandlerList<T, X> handlerList = getDualHandlerList(event);
            handlerList.put(handler);
        }
    }

    public <T, X> void on(Class<T> event, DualHandler<T, X> handler) {
        synchronized (dualHandlers) {
            DualHandlerList<T, X> handlerList = getDualHandlerList(event);
            handlerList.put(handler);
        }
    }

    public void emit(String event, Object arg1, Object arg2) {
        emit((Object) event, arg1, arg2);
    }

    public void emit(Class event, Object arg1, Object arg2) {
        emit((Object) event, arg1, arg2);
    }

    public void emit(String event, Object arg) {
        emit((Object) event, arg);
    }

    public void emit(Class event, Object arg) {
        emit((Object) event, arg);
    }

    public void emit(Object event, Object arg) {
        boolean handle = false;
        HandlerList handlerList = null;
        synchronized (handlers) {
            handlerList = handlers.get(event);
            if (handlerList == null) {
                // also check dual handlers before logging the warning since we might want to emit the same key with both handler types (like AsyncObjectChannel)
                if(dualHandlers.get(event) == null) {
                    logger.warn("unhandled event detected: " + event);
                }
            } else if (handlerList.isEmpty()) {
                handlers.remove(event);
            } else {
                handle = true;
            }
        }
        if (handle) {
            handlerList.handle(arg);    // cant be null fuck you netbeans
        }

    }

    public void emit(Object event, Object arg1, Object arg2) {
        boolean handle = false;
        DualHandlerList handlerList;
        synchronized (dualHandlers) {
            handlerList = dualHandlers.get(event);
            if (handlerList == null) {
                if(handlers.get(event) == null) {   // also check on handlers before logging the warning since we might want to emit the same key with both handler types (like AsyncObjectChannel)
                    logger.warn("unhandled event detected: " + event);
                }
            } else if (handlerList.isEmpty()) {
                dualHandlers.remove(event);
            } else {
                handle = true;
            }
        }
        if (handle) {
            handlerList.handle(arg1, arg2); // also cant be null fuck you netbeans
        }
    }

}
