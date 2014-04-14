/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.core;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.jetbrains.annotations.NotNull;

/**
 * # YokeException
 *
 * A YokeException is a Exception that can be link to a specific Http Status Code without the need to handle it in code.
 * Error Handlers are expected to use the supplied status code.
 */
public class YokeException extends Throwable {

    private static final long serialVersionUID = 1L;

    private final Number code;

    public YokeException(@NotNull Number code) {
        this(code, HttpResponseStatus.valueOf(code.intValue()).reasonPhrase());
    }

    public YokeException(@NotNull Number code, @NotNull String message) {
        super(message);
        this.code = code;
    }

    public YokeException(@NotNull Number code, @NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public YokeException(@NotNull Number code, @NotNull String message, @NotNull String cause) {
        super(message, new RuntimeException(cause));
        this.code = code;
    }

    public YokeException(@NotNull String message) {
        super(message);
        this.code = 500;
    }

    public YokeException(@NotNull Number code, @NotNull Throwable cause) {
        super(cause);
        this.code = code;
    }

    public Number getErrorCode() {
        return code;
    }
}
