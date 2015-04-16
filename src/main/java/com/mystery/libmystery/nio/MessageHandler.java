package com.mystery.libmystery.nio;

import java.io.Serializable;

@FunctionalInterface
public interface MessageHandler<T extends Serializable> extends Handler<T> {

}
