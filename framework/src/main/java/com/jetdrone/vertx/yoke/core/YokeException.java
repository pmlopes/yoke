// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.core
package com.jetdrone.vertx.yoke.core;

import io.netty.handler.codec.http.HttpResponseStatus;

// # YokeException
//
// A YokeException is a Exception that can be link to a specific Http Status Code without the need to handle it in code.
// Error Handlers are expected to use the supplied status code.
public class YokeException extends Throwable {

    private static final long serialVersionUID = 1L;

    private final Number code;

    public YokeException(Number code) {
        this(code, HttpResponseStatus.valueOf(code.intValue()).reasonPhrase());
    }

    public YokeException(Number code, String message) {
        super(message);
        this.code = code;
    }

    public YokeException(Number code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public YokeException(Number code, String message, String cause) {
        super(message, new Throwable(cause));
        this.code = code;
    }

    public YokeException(String message) {
        super(message);
        this.code = 500;
    }

    public YokeException(Number code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public Number getErrorCode() {
        return code;
    }
}
