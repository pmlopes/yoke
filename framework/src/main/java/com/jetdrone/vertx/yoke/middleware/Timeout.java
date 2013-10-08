// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.middleware
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;

// # Timeout
//
// Times out the request in ```ms```, defaulting to ```5000```.
//
// The timeout error is passed to ```next.handle(408)``` so that you may customize the response behaviour.
public class Timeout extends Middleware {

    private final long timeout;

    public Timeout(long timeout) {
        this.timeout = timeout;
    }

    public Timeout() {
        this(5000);
    }
    @Override
    public void handle(final YokeRequest request, final Handler<Object> next) {
        final YokeResponse response = request.response();

        final long timerId = vertx.setTimer(timeout, new Handler<Long>() {
            @Override
            public void handle(Long event) {
                next.handle(408);
            }
        });

        response.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                vertx.cancelTimer(timerId);
            }
        });

        next.handle(null);
    }
}
