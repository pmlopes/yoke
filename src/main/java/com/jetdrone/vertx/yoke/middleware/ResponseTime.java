package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class ResponseTime extends Middleware {
    @Override
    public void handle(HttpServerRequest request, Handler<Object> next) {

        final long start = System.currentTimeMillis();
        final YokeHttpServerResponse res = (YokeHttpServerResponse) request.response();

        res.headersHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                long duration = System.currentTimeMillis() - start;
                res.putHeader("x-response-time", duration + "ms");
            }
        });

        next.handle(null);
    }
}
