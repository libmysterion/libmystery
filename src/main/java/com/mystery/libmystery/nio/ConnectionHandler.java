package com.mystery.libmystery.nio;

@FunctionalInterface
public interface ConnectionHandler {
    public void onConnected(AsynchronousObjectSocketChannel client);    
}
