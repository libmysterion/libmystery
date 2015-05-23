package com.mystery.libmystery.injection;

public final class InjectorFactory {

    private static Injector instance;

    public static Injector getInstance() {
        if (instance == null) {
            instance = new Injector();
        }
        return instance;
    }
}
