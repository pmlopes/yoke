package com.jetdrone.vertx.yoke.middleware.groovy;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.middleware.YokeHttpServerResponse;
import groovy.lang.Closure;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerResponse;

import java.util.Map;

public class GYokeHttpServerResponse extends YokeHttpServerResponse {
    public GYokeHttpServerResponse(HttpServerResponse response, Map<String, Object> renderContext, Map<String, Engine> renderEngines) {
        super(response, renderContext, renderEngines);
    }

    // TODO: render

    public Map<String, Object> getHeaders() {
        return headers();
    }

    public Map<String, Object> getTrailers() {
        return trailers();
    }

    public GYokeHttpServerResponse closeHandler(final Closure<Void> handler) {
        closeHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                handler.call(event);
            }
        });
        return this;
    }

    public GYokeHttpServerResponse drainHandler(final Closure<Void> handler) {
        drainHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                handler.call(event);
            }
        });
        return this;
    }

    public GYokeHttpServerResponse exceptionHandler(final Closure<Throwable> handler) {
        exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable event) {
                handler.call(event);
            }
        });
        return this;
    }
}
