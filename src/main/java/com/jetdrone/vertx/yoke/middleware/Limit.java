package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class Limit extends Middleware {

    private final long limit;

    public Limit(long limit) {
        this.limit = limit;
    }

    @Override
    public void handle(final HttpServerRequest request, final Handler<Object> next) {
        // inside middleware the original request has been wrapped with yoke's
        // implementation
        final YokeHttpServerRequest req = (YokeHttpServerRequest) request;

        if (req.hasBody()) {
            req.setBodyLengthLimit(limit);

            long len = req.contentLength();
            // limit by content-length
            if (len > limit) {
                next.handle(413);
                return;
            }
        }

        next.handle(null);
    }
}
