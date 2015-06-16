package com.jetdrone.vertx.yoke.middleware;

import groovy.lang.Closure;
import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;

public class GCRUD extends CRUD {

    private static Handler wrapClosure(final Closure closure) {
        final int params = closure.getMaximumNumberOfParameters();
        if (params != 2) {
            throw new RuntimeException("Cannot infer the closure signature, should be: filter [, next]");
        }

        return new Handler() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull JsonObject filter, @NotNull final org.vertx.java.core.Handler<JsonObject> next) {
                closure.call(filter.toMap(), request, new org.vertx.java.core.Handler<Map<String, Object>>() {
                    @Override
                    public void handle(Map<String, Object> json) {
                        next.handle(new JsonObject(json));
                    }
                });
            }
        };
    }


    public GCRUD createHandler(Closure handler) {
        this.createHandler = wrapClosure(handler);
        return this;
    }

    public GCRUD readHandler(Closure handler) {
        this.readHandler = wrapClosure(handler);
        return this;
    }

    public GCRUD updateHandler(Closure handler) {
        this.updateHandler = wrapClosure(handler);
        return this;
    }

    public GCRUD deleteHandler(Closure handler) {
        this.deleteHandler = wrapClosure(handler);
        return this;
    }
}
