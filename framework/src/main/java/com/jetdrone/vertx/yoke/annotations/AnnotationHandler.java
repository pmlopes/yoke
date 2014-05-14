package com.jetdrone.vertx.yoke.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface AnnotationHandler<T> {

    boolean isFor(Class<?> clazz);

    void process(final T router, final Object instance, final Class<?> clazz, final Method method);

    void process(final T router, final Object instance, final Class<?> clazz, final Field field);
}
