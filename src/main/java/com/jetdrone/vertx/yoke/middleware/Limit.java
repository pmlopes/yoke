package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;

public class Limit extends Middleware {

    private final long limit;

    public Limit(long limit) {
        this.limit = limit;
    }

    @Override
    public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
        if (request.hasBody()) {
            request.setBodyLengthLimit(limit);

            long len = request.contentLength();
            // limit by content-length
            if (len > limit) {
                next.handle(413);
                return;
            }
        }

        next.handle(null);
    }
}
