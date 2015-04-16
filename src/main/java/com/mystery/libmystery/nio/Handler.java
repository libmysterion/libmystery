package com.mystery.libmystery.nio;


@FunctionalInterface
interface Handler<T> {
  
    public void handle(T t);

}
