package com.jetdrone.vertx.yoke.annotations.processors;

import com.jetdrone.vertx.yoke.annotations.AnnotationHandler;

public abstract class AbstractAnnotationHandler<T> implements AnnotationHandler<T> {

    private final Class<T> clazz;

    public AbstractAnnotationHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean isFor(Class<?> clazz) {
        return this.clazz.isAssignableFrom(clazz);
    }

}
