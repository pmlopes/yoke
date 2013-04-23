package com.jetdrone.vertx.kitcms;

import org.vertx.java.core.AsyncResult;

public class FutureAsyncResult<T> implements AsyncResult<T> {

    private final T result;
    private final Throwable exception;

    public FutureAsyncResult(Throwable exception, T result) {
        this.exception = exception;
        this.result = result;
    }

    @Override
    public T result() {
        return result;
    }

    @Override
    public Throwable cause() {
        return exception;
    }

    @Override
    public boolean succeeded() {
        return exception == null;
    }

    @Override
    public boolean failed() {
        return exception != null;
    }
}
