package com.mystery.libmystery.nio;

@FunctionalInterface
public interface MessageHandler<T> {
    public void handleMessage(T message);
}
