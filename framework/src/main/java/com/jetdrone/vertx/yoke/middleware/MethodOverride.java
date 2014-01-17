// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.middleware
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.json.JsonObject;

// # MethodOverride
//
// Pass an optional ```key``` to use when checking for a method override, othewise defaults to *_method* or the header
// *x-http-method-override*. The original method is available via ```req.originalMethod```.
//
// @note If the method override is sent in a *POST* then the [BodyParser](BodyParser.html) middleware must be used and
// installed prior this one.
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

        // other methods than GET, HEAD and OPTIONS may have body
        if (!"GET".equals(request.method()) && !"HEAD".equals(request.method()) && !"OPTIONS".equals(request.method())) {
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

            final JsonObject json = request.body();
            if (json != null) {
                String method = json.getString(key);
                if (method != null) {
                    json.removeField(key);
                    request.setMethod(method);
                    next.handle(null);
                    return;
                }
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
