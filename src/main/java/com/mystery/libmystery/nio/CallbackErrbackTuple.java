
package com.mystery.libmystery.nio;

public class CallbackErrbackTuple {

    private final Object sync = new Object();

    private Errback errback;
    private Runnable callback;

    private Throwable error;
    private boolean done = false;

    public CallbackErrbackTuple onError(Errback errback) {

        synchronized (sync) {
            if (this.error != null) {
                errback.onFailure(error);
            } else {
                this.errback = errback;
            }
        }
        return this;
    }

    public CallbackErrbackTuple onSucess(Runnable callback) {
        synchronized (sync) {
            if (this.done) {
                callback.run();
            } else {
                this.callback = callback;
            }
        }
        return this;
    }

    void doError(Throwable t) {
        synchronized (sync) {
           // System.out.println("doError" + (this.errback == null ? " null callback" : " has callback"));
            if (this.errback != null) {
                this.errback.onFailure(t);
            } else {
                this.error = t;
            }
        }

    }

    void doSuccess() {
        synchronized (sync) {
            //System.out.println("doSuccess" + (this.callback == null ? " null callback" : " has callback"));
            if (this.callback != null) {
                this.callback.run();
            } else {
                this.done = true;
            }
        }

    }
}
