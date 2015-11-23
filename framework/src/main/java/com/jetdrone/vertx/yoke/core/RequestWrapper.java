/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.core;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.store.SessionStore;
import io.vertx.core.http.HttpServerRequest;

import java.util.Map;

/**
 * # RequestWrapper
 *
 * Interface to wrap request objects to language specific implementations. This is an internal interface and should
 * not be used on a normal Yoke application.
 */
public interface RequestWrapper {

    /**
     * For other language bindings this method can be override.
     *
     * @param request The Vertx HttpServerRequest
     * @param context The request context a map where data can be temporarily stored during the lifespan of the request
     * @param engines the current list of render engines (this is an unmodifiable map)
     * @param store the current Session Store implementation
     * @return an Implementation of YokeRequest
     */
    YokeRequest wrap(HttpServerRequest request, Context context, Map<String, Engine> engines, SessionStore store);
}
