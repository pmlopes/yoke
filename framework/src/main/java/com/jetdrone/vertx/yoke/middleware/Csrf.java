// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.middleware
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;

import java.util.UUID;

public class Csrf extends Middleware {

    final ValueHandler valueHandler;
    final String key;

    public Csrf(final String key) {
        this.key = key;
        valueHandler = new ValueHandler() {
            @Override
            public String handle(YokeRequest request) {
                String token = request.formAttributes().get(key);
                if (token == null) {
                    token = request.params().get(key);
                    if (token == null) {
                        token = request.headers().get("x-csrf-token");
                    }
                }

                return token;
            }
        };
    }

    public Csrf() {
        this("_csrf");
    }

    public Csrf(String key, ValueHandler valueHandler) {
        this.key = key;
        this.valueHandler = valueHandler;
    }

    public Csrf(ValueHandler valueHandler) {
        this("_csrf", valueHandler);
    }

    public interface ValueHandler {
        String handle(YokeRequest request);
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {

        String token = request.get(key);
        // generate CSRF token
        if (token == null) {
            token = UUID.randomUUID().toString();
            request.put(key, token);
        }

        // ignore these methods
        if ("GET".equals(request.method()) || "HEAD".equals(request.method()) || "OPTIONS".equals(request.method())) {
            next.handle(null);
            return;
        }

        // expect multipart
        request.expectMultiPart(true);

        // determine value
        String val = valueHandler.handle(request);

        // check
        if (!token.equals(val)) {
            next.handle(403);
            return;
        }

        // OK
        next.handle(null);
    }

}
