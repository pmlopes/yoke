/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.filters.DeflateWriterFilter;
import com.jetdrone.vertx.yoke.middleware.filters.GZipWriterFilter;
import io.vertx.core.http.HttpMethod;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * # Compress
 *
 * Middleware to compress responses and set the appropriate response headers.
 * Not all responses are compressed, the middleware first inspects if the
 * request accepts compression and tries to select the best matched algorithm.
 *
 * You can specify which content types are compressable and by default json/text/javascript
 * are enabled.
 */
public class Compress extends Middleware {

    /**
     * Regular expression to identify resources that are subject to compression
     */
    private final Pattern filter;

    /**
     * Creates a new Compression Middleware given a regular expression of allowed mime types
     *
     * @param filter Regular expression to specify which mime types are allowed to be compressed
     */
    public Compress(@NotNull final Pattern filter) {
        this.filter = filter;
    }

    /**
     * Creates a new Compression Middleware using the default allowed mime types
     */
    public Compress() {
        this(Pattern.compile("json|text|javascript"));
    }

    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
        final HttpMethod method = request.method();
        final YokeResponse response = request.response();

        // vary
        response.putHeader("vary", "accept-encoding");

        // head requests are not compressed
        if (HttpMethod.HEAD == method) {
            next.handle(null);
            return;
        }

        final String accept = request.getHeader("accept-encoding");

        // if no accept then there is no need to filter
        if (accept == null) {
            next.handle(null);
            return;
        }

        try {
            // default to gzip
            if ("*".equals(accept.trim())) {
                response.setFilter(new GZipWriterFilter(filter));
            } else {
                if (accept.contains("gzip")) {
                    response.setFilter(new GZipWriterFilter(filter));
                } else if (accept.contains("deflate")) {
                    response.setFilter(new DeflateWriterFilter(filter));
                }
            }
            next.handle(null);
        } catch (IOException ioe) {
            next.handle(ioe);
        }
    }
}
