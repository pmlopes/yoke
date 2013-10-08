// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.middleware
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.json.JsonObject;

public class MethodOverride extends Middleware {

    private final String key;

    public MethodOverride(String key) {
        this.key = key;
    }

    public MethodOverride() {
        this("_method");
    }

    @Override
    public void handle(final YokeRequest request, final Handler<Object> next) {

        // expect multipart
        request.expectMultiPart(true);

        final MultiMap urlEncoded = request.formAttributes();

        if (urlEncoded != null) {
            String method = urlEncoded.get(key);
            if (method != null) {
                urlEncoded.remove(key);
                request.setMethod(method);
                next.handle(null);
                return;
            }
        }

        final JsonObject json = request.jsonBody();
        if (json != null) {
            String method = json.getString(key);
            if (method != null) {
                json.removeField(key);
                request.setMethod(method);
                next.handle(null);
                return;
            }
        }

        String xHttpMethodOverride = request.getHeader("x-http-setMethod-override");

        if (xHttpMethodOverride != null) {
            request.setMethod(xHttpMethodOverride);
        }

        // if reached the end continue to the next middleware
        next.handle(null);
    }
}
