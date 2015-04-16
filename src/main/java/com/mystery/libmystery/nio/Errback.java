package com.mystery.libmystery.nio;

@FunctionalInterface
public interface Errback {
    
    public void onFailure(Throwable error);

}
