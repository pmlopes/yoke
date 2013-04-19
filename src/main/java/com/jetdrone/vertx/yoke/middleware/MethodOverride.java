package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;

public class MethodOverride extends Middleware {

    private final String key;

    public MethodOverride(String key) {
        this.key = key;
    }

    public MethodOverride() {
        this("_method");
    }

    @Override
    public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
        final Object body = request.body();

        if (request.hasBody() && body != null) {
            if (body instanceof Map) {
                Object method = ((Map) body).get(key);
                if (method != null) {
                    if (method instanceof String) {
                        ((Map) body).remove(key);
                        request.method((String) method);
                    }
                }
            } else if (body instanceof JsonObject) {
                String method = ((JsonObject) body).getString(key);
                if (method != null) {
                    ((JsonObject) body).removeField(key);
                    request.method(method);
                }
            }
        } else {
            String xHttpMethodOverride = request.headers().get("x-http-method-override");

            if (xHttpMethodOverride != null) {
                request.method(xHttpMethodOverride);
            }
        }

        next.handle(null);
    }
}
