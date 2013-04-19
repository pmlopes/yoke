package com.jetdrone.vertx.yoke;

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;

import java.util.Map;

public abstract class Engine {

    protected Vertx vertx;

    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    public abstract void render(final String template, final Map<String, Object> context, final AsyncResultHandler<Buffer> asyncResultHandler);
}
