/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.core.impl.ThreadLocalUTCDateFormat;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.SocketAddress;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpVersion;

import java.util.Date;

/** # Logger
 *
 * Logger for request. There are 3 formats included:
 * 1. DEFAULT
 * 2. SHORT
 * 3. TINY
 *
 * Default tries to log in a format similar to Apache log format, while the other 2 are more suited to development mode.
 * The logging depends on Vert.x logger settings and the severity of the error, so for errors with status greater or
 * equal to 500 the fatal severity is used, for status greater or equal to 400 the error severity is used, for status
 * greater or equal to 300 warn is used and for status above 100 info is used.
 */
public class Logger extends Middleware {

    private final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The Date formatter (UTC JS compatible format)
     */
    private final ThreadLocalUTCDateFormat ISODATE;

    /**
     * The possible out of the box formats.
     */
    public enum Format {
        DEFAULT,
        SHORT,
        TINY
    }

    /** log before request or after
     */
    private final boolean immediate;

    /** the current choosen format
     */
    private final Format format;

    public Logger(final boolean immediate, @NotNull Format format) {
        this.immediate = immediate;
        this.format = format;

        ISODATE = new ThreadLocalUTCDateFormat();
    }

    public Logger(@NotNull Format format) {
        this(false, format);
    }

    public Logger() {
        this(false, Format.DEFAULT);
    }

    private String getVersionString(HttpVersion version) {
        switch (version) {
            case HTTP_1_1:
                return "HTTP/1.1";
            case HTTP_1_0:
                return "HTTP/1.0";
        }
        return null;
    }

    private String getClientAddress(SocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return null;
        }

        return inetSocketAddress.host();
    }

    private void log(YokeRequest request, long timestamp, final String remoteClient, final String version, final String method, final String uri) {
        long contentLength = 0;
        if (immediate) {
            Object obj = request.getHeader("content-length");
            if (obj != null) {
                contentLength = Long.parseLong(obj.toString());
            }
        } else {
            Object obj = request.response().getHeader("content-length");
            if (obj != null) {
                contentLength = Long.parseLong(obj.toString());
            }
        }

        int status = request.response().getStatusCode();
        String message = null;

        switch (format) {
            case DEFAULT:
                Object referrer = request.getHeader("referrer", "");
                Object userAgent = request.getHeader("user-agent", "");

                message = String.format("%s - - [%s] \"%s %s %s\" %d %d \"%s\" \"%s\"",
                        remoteClient,
                        ISODATE.format(new Date(timestamp)),
                        method,
                        uri,
                        version,
                        status,
                        contentLength,
                        referrer,
                        userAgent);
                break;
            case SHORT:
                message = String.format("%s - %s %s %s %d %d - %d ms",
                        remoteClient,
                        method,
                        uri,
                        version,
                        status,
                        contentLength,
                        (System.currentTimeMillis() - timestamp));
                break;
            case TINY:
                message = String.format("%s %s %d %d - %d ms",
                        method,
                        uri,
                        status,
                        contentLength,
                        (System.currentTimeMillis() - timestamp));
                break;
        }

        logMessage(status, message);
    }

    protected void logMessage(int status, String message)
    {
        if (status >= 500) {
            logger.fatal(message);
        } else if (status >= 400) {
            logger.error(message);
        } else if (status >= 300) {
            logger.warn(message);
        } else {
            logger.info(message);
        }
    }

    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {

        // common logging data
        final long timestamp = System.currentTimeMillis();
        final String remoteClient = getClientAddress(request.remoteAddress());
        final HttpMethod method = request.method();
        final String uri = request.uri();
        final String version = getVersionString(request.version());

        if (immediate) {
            log(request, timestamp, remoteClient, version, method.name(), uri);
        } else {
            request.response().endHandler(new Handler<Void>() {
                @Override
                public void handle(Void event) {
                    log(request, timestamp, remoteClient, version, method.name(), uri);
                }
            });
        }

        next.handle(null);
    }
}
