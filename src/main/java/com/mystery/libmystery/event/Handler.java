package com.mystery.libmystery.event;


@FunctionalInterface
public interface Handler<T> {
  
    public void handle(T t);

}
