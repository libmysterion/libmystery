
package com.mystery.libmystery.nio;

import java.util.ArrayList;
import java.util.List;


public class AsyncOperation<T> {
    
    private List<Callback<T>> callbacks = new ArrayList<>();
    private List<Errback> errbacks = new ArrayList<>();
    
    public void onSuccess(Callback<T> callback){
        this.callbacks.add(callback);
    }
    
    public void onFailure(Errback errback){
        this.errbacks.add(errback);
    }
    
    void notifySuccessHandlers(T result){  
        this.callbacks.forEach((callback) -> callback.onSuccess(result));
    }
    
    void notifyFailureHandlers (Throwable throwable) {
        this.errbacks.forEach((callback) -> callback.onFailure(throwable));
    }
}
