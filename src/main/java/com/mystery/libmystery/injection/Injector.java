package com.mystery.libmystery.injection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Injector {

    public static final Logger log = LoggerFactory.getLogger(Injector.class);

    private final HashMap<Class, Object> singletons = new HashMap<>();
    private final HashMap<Class, Function<Class, Object>> instanceFactories = new HashMap<>();
    private Function<String, Object> propertySource = (s) -> null;

    public void setPropertySource(Function<String, Object> propertySource) {
        this.propertySource = propertySource;
    }

    public <T> void setInstanceFactory(Class<T> clazz, Function<Class<T>, T> instanceFactory) {
        this.instanceFactories.put((Class) clazz, (Function) instanceFactory);
    }

    private <T> void cacheSingleton(T object) {
        Singleton singletonAnnotation = object.getClass().getAnnotation(Singleton.class);
        if (singletonAnnotation != null) {
            singletons.put(object.getClass(), object);
        }
    }

    private void injectObject(Object injectee) throws IllegalAccessException {

        Field[] declaredFields = injectee.getClass().getDeclaredFields();
        if (declaredFields != null) {
            for (Field field : declaredFields) {
                injectFields(field, injectee); 
                injectProperties(field, injectee);
            }
        }
    }

    private void injectFields(Field field, Object injectee) throws IllegalAccessException {
        if (field.getAnnotation(Inject.class) != null) {
            Class fieldClass = field.getType();
            Object singleton = singletons.get(fieldClass);
            if (singleton != null) {
                setField(field, singleton, injectee);
            } else {
                setField(field, create(fieldClass), injectee);
            }
        }
    }
    
   
    private void injectProperties(Field field, Object injectee) throws IllegalAccessException {
        Property annotation = field.getAnnotation(Property.class);
        if (annotation != null) {
            String propertyKey = annotation.value().isEmpty() ? field.getName() : annotation.value();
            Object propertyValue = propertySource.apply(propertyKey);
            setField(field, propertyValue, injectee);
        }
    }

    private void setField(Field field, Object fieldValue, Object fieldOwner) throws IllegalAccessException {
        boolean originalAccessible = field.isAccessible();

        try {
            field.setAccessible(true);
            field.set(fieldOwner, fieldValue);
        } finally {
            field.setAccessible(originalAccessible);
        }
    }

    public final <T> T create(Class<T> clazz) {
        if (clazz.getAnnotation(Singleton.class) != null) {
            T singleton = (T) singletons.get(clazz);
            if (singleton != null) {
                return singleton;
            }
        }

        T instance = (T) createInstanceWithFactory(clazz);

        try {
            injectObject(instance);
        } catch (IllegalAccessException ex) {
            log.error("Could not instantiate class : " + clazz.getSimpleName(), ex);
            throw new InjectionException("Could not instantiate class : " + clazz.getSimpleName(), ex);
        }
       
        invokePostConstruct(instance);
       
        cacheSingleton(instance);
       
        return instance;

    }

    private <T> T createInstanceWithFactory(Class<T> clazz) {
        Function<Class, Object> factory = instanceFactories.get(clazz);
        if (factory == null) {
            factory = (c) -> createInstance(c);
            instanceFactories.put(clazz, factory);
        }
        return (T) factory.apply(clazz);
    }

    private <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            log.error("Could not instantiate class : " + clazz.getSimpleName(), ex);
            throw new InjectionException("Could not instantiate class : " + clazz.getSimpleName(), ex);
        }
    }

    private <T> void invokePostConstruct(T instance)  {
        Method[] methods = instance.getClass().getDeclaredMethods();
        for(Method method : methods){
            if(method.getAnnotation(PostConstruct.class) != null){
                boolean originalAccessible = method.isAccessible();
                try {
                    method.setAccessible(true);
                    method.invoke(instance);
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {
                    log.error("Error invoking @PostConstruct on class" + instance.getClass().getName(), ex);
                    throw new InjectionException(ex);
                } finally {
                     method.setAccessible(originalAccessible);
                }            
            } 
        }
    }

    /*
    * This API allows us to choose to cache a singleton that does not have the @Singleton annotation e.g. third party API objects
    */
    public <T> void setSingleton(Class<T> clazz, T instance) {
        this.singletons.put(clazz, instance);
    }

}

class InjectionException extends RuntimeException {

    public InjectionException(String string) {
        super(string);
    }

    public InjectionException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public InjectionException(Throwable thrwbl) {
        super(thrwbl);
    }

    

        
}