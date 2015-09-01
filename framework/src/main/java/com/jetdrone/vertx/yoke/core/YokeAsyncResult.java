/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.core;

import org.jetbrains.annotations.NotNull;
import io.vertx.core.AsyncResult;

/**
 * # YokeAsyncResult
 *
 * Class to wrap a pair of Throwable + Result into a Vert.x AsyncResult Object.
 */
public class YokeAsyncResult<T> implements AsyncResult<T> {
    final Throwable throwable;
    final T result;

    /** Create a YokeAsyncResult given an error object and a result in the same style as CPS. This is useful for
     * scripting engines where data types do not have to match java types.
     *
     * @param error error object null for success
     * @param result operation result any
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

    /** Create a new YokeAsyncResult given a Throwable error. This is always a failure result.
     *
     * @param throwable the error that was thrown.
     */
    public YokeAsyncResult(@NotNull Throwable throwable) {
        this.throwable = throwable;
        this.result = null;
    }

    /** Create a new YokeAsyncResult given a T result. This is always a successful result.
     *
     * @param result the result from the operation.
     */
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
