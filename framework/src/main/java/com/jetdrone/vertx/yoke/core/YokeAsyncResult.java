// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.core
package com.jetdrone.vertx.yoke.core;

import org.vertx.java.core.AsyncResult;

// # YokeAsyncResult
public class YokeAsyncResult<T> implements AsyncResult<T> {
    final Throwable throwable;
    final T result;

    /**
     * Used from scripting engines
     */
    public YokeAsyncResult(Object error, T result) {
        if (error != null) {
            if (error instanceof Throwable) {
                this.throwable = (Throwable) error;
            } else if (error instanceof Number) {
                this.throwable = new YokeException((Number) error);
            } else {
                this.throwable = new YokeException(error.toString());
            }
        } else {
            this.throwable = null;
        }
        this.result = result;
    }

    public YokeAsyncResult(Throwable throwable) {
        this.throwable = throwable;
        this.result = null;
    }

    public YokeAsyncResult(T result) {
        this.throwable = null;
        this.result = result;
    }

    @Override
    public T result() {
        return result;
    }

    @Override
    public Throwable cause() {
        return throwable;
    }

    @Override
    public boolean succeeded() {
        return throwable == null;
    }

    @Override
    public boolean failed() {
        return throwable != null;
    }
}
