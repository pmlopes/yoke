/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.core;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.vertx.java.core.http.HttpServerRequest;

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
     * @param secure Is the server SSL?
     * @param engines the current list of render engines (this is an unmodifiable map)
     * @return an Implementation of YokeRequest
     */
    YokeRequest wrap(HttpServerRequest request, boolean secure, Map<String, Engine> engines);
}
