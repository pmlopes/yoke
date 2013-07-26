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
package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;

/**
 * Abstract class that needs to be implemented when creating a new middleware piece.
 * The class provides access to the Vertx object and by default is not marked as a error handler middleware.
 *
 * If there is a need to create a new error handler middleware the isErrorHandler method should be overridden to
 * return true.
 */
public abstract class Middleware {

    protected Vertx vertx;

    protected Logger logger;

    public Middleware init(Vertx vertx, Logger logger) {
        this.vertx = vertx;
        this.logger = logger;

        return this;
    }

    public boolean isErrorHandler() {
        return false;
    }

    /**
     * Handles a request.
     *
     * @param request A YokeRequest which in practice is a extended HttpServerRequest
     * @param next The callback to inform that the next middleware in the chain should be used. A value different from
     *             null represents an error and in that case the error handler middleware will be executed.
     */
    public abstract void handle(final YokeRequest request, final Handler<Object> next);
}
