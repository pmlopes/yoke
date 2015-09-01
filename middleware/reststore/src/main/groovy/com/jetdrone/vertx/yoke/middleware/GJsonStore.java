package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.IMiddleware;
import groovy.lang.Closure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.vertx.core.Handler;

public class GJsonStore extends JsonStore {

    private static IMiddleware wrapClosure(final Closure closure) {
        final int params = closure.getMaximumNumberOfParameters();
        return new IMiddleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                if (params == 1) {
                    closure.call(request);
                } else if (params == 2) {
                    closure.call(request, next);
                } else {
                    throw new RuntimeException("Cannot infer the closure signature, should be: request [, next]");
                }
            }
        };
    }

    public GJsonStore() {
        super("/api");
    }

    public GJsonStore(String prefix) {
        super(prefix, null);
    }

    public GJsonStore(String prefix, String sortParam) {
        super(prefix, sortParam);
    }

    public GCRUD collection(@NotNull final String name, @Nullable final Closure validator) {
        return (GCRUD) collection(name, "id", wrapClosure(validator));
    }

    public GCRUD collection(@NotNull final String name, @NotNull final String key, @Nullable final Closure validator) {
        return (GCRUD) collection(name, key, wrapClosure(validator));
    }

    public GCRUD collection(@NotNull final String name, @NotNull final String key, @NotNull final GCRUD crud, @Nullable final Closure validator) {
        collection(name, key, crud, wrapClosure(validator));
        return crud;
    }

    @Override
    protected CRUD createCrud() {
        return new GCRUD();
    }
}
