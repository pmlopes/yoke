package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.Handler;

public final class CacheControl extends Middleware {

    @Override
    public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        request.response().putHeader("Cache-Control", "no-store, no-cache");
        next.handle(null);
    }
}
