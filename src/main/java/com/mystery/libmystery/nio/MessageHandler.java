package com.mystery.libmystery.nio;

import com.mystery.libmystery.event.Handler;
import java.io.Serializable;

@FunctionalInterface
public interface MessageHandler<T extends Serializable> extends Handler<T> {

}
