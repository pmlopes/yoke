/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import io.vertx.core.http.HttpMethod;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

/** # MethodOverride
 *
 * Pass an optional ```key``` to use when checking for a method override, othewise defaults to *_method* or the header
 * *x-http-method-override*. The original method is available via ```req.originalMethod```.
 *
 * *note:* If the method override is sent in a *POST* then the [BodyParser](BodyParser.html) middleware must be used and
 * installed prior this one.
 */
public class MethodOverride extends Middleware {

    private final String key;

    public MethodOverride(@NotNull final String key) {
        this.key = key;
    }

    public MethodOverride() {
        this("_method");
    }

    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {

        // other methods than GET, HEAD and OPTIONS may have body
        if (HttpMethod.GET != request.method() && HttpMethod.HEAD != request.method() && HttpMethod.OPTIONS != request.method()) {
            // expect multipart
            request.setExpectMultipart(true);

            final MultiMap urlEncoded = request.formAttributes();

            if (urlEncoded != null) {
                String method = urlEncoded.get(key);
                if (method != null) {
                    urlEncoded.remove(key);
                    request.setMethod(HttpMethod.valueOf(method));
                    next.handle(null);
                    return;
                }
            }

            final JsonObject json = request.body();
            if (json != null) {
                String method = json.getString(key);
                if (method != null) {
                    json.remove(key);
                    request.setMethod(HttpMethod.valueOf(method));
                    next.handle(null);
                    return;
                }
            }
        }

        String xHttpMethodOverride = request.getHeader("x-http-setmethod-override");

        if (xHttpMethodOverride != null) {
            request.setMethod(HttpMethod.valueOf(xHttpMethodOverride));
        }

        // if reached the end continue to the next middleware
        next.handle(null);
    }
}
