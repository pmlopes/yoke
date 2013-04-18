package com.jetdrone.vertx.yoke;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;

public abstract class Middleware {

    protected Vertx vertx;

    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    public boolean isErrorHandler() {
        return false;
    }

    public abstract void handle(final HttpServerRequest request, final Handler<Object> next);
}
