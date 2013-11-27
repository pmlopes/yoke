// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.middleware
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.filters.DeflateWriterFilter;
import com.jetdrone.vertx.yoke.middleware.filters.GZipWriterFilter;
import org.vertx.java.core.Handler;

import java.io.IOException;
import java.util.regex.Pattern;

public class Compress extends Middleware {

    private final Pattern filter;

    public Compress(Pattern filter) {
        this.filter = filter;
    }

    public Compress() {
        this(Pattern.compile("json|text|javascript"));
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        final String method = request.method();
        final YokeResponse response = request.response();

        // vary
        response.putHeader("vary", "accept-encoding");

        // head requests are not compressed
        if ("HEAD".equals(method)) {
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
