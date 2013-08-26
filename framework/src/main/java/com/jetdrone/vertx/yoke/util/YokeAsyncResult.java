/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetdrone.vertx.yoke.util;

import org.vertx.java.core.AsyncResult;

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
