package com.jetdrone.vertx.yoke.annotations;

import com.jetdrone.vertx.yoke.middleware.Router;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface AnnotationHandler {

    void process(final Router router, final Object instance, final Class clazz, final Method method);

    void process(final Router router, final Object instance, final Class clazz, final Field field);
}
