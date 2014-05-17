package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;

public final class CacheControl extends Middleware {

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        request.response().putHeader("Cache-Control", "no-store, no-cache");
        next.handle(null);
    }
}
