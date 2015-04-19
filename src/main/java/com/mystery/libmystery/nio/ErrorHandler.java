package com.mystery.libmystery.nio;

import com.mystery.libmystery.event.Handler;

@FunctionalInterface
public interface ErrorHandler extends Handler<Throwable> {
    
}
