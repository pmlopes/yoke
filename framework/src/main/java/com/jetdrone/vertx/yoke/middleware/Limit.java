/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;

/**
 * # Limit
 *
 * Limits the request body to a specific amount of bytes. If the request body contains more bytes than the allowed
 * limit an *413* error is sent back to the client.
 */
public class Limit extends Middleware {

    /**
     * The max allowed length for the resource
     */
    private final long limit;

    /**
     * Creates a Limit instance with a given max allowed bytes
     *
     * <pre>
     * new Yoke(...)
     *   .use(new Limit(1024));
     * </pre>
     *
     * @param limit
     */
    public Limit(final long limit) {
        this.limit = limit;
    }

    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
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
