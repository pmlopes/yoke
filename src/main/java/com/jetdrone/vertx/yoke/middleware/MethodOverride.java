package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
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
    public void handle(HttpServerRequest request, Handler<Object> next) {
        // inside middleware the original request has been wrapped with yoke's
        // implementation
        final YokeHttpServerRequest req = (YokeHttpServerRequest) request;
        final Object body = req.body();

        if (req.hasBody() && body != null) {
            if (body instanceof Map) {
                Object method = ((Map) body).get(key);
                if (method != null) {
                    if (method instanceof String) {
                        ((Map) body).remove(key);
                        req.method((String) method);
                    }
                }
            } else if (body instanceof JsonObject) {
                String method = ((JsonObject) body).getString(key);
                if (method != null) {
                    ((JsonObject) body).removeField(key);
                    req.method(method);
                }
            }
        } else {
            String xHttpMethodOverride = req.headers().get("x-http-method-override");

            if (xHttpMethodOverride != null) {
                req.method(xHttpMethodOverride);
            }
        }

        next.handle(null);
    }
}
