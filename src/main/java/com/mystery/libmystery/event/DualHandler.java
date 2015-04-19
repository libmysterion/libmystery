package com.mystery.libmystery.event;

@FunctionalInterface
public interface DualHandler<A, B> {

    public void handle(A a, B b);
}
