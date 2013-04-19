package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;

public class ResponseTime extends Middleware {
    @Override
    public void handle(YokeHttpServerRequest request, Handler<Object> next) {

        final long start = System.currentTimeMillis();
        final YokeHttpServerResponse response = request.response();

        response.headersHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                long duration = System.currentTimeMillis() - start;
                response.putHeader("x-response-time", duration + "ms");
            }
        });

        next.handle(null);
    }
}
