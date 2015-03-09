
package com.mystery.libmystery.nio;


@FunctionalInterface
public interface Callback<T> {
    
    public void onSuccess(T result);
}
