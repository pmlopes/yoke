/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.core;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.Map;

// # RequestWrapper
//
// Interface to wrap request objects to language specific implementations. This is an internal interface and should
// not be used on a normal Yoke application.
// @internal
public interface RequestWrapper {

    // For other language bindings this method can be override.
    //
    // @method wrap
    // @param {HttpServerRequest} request The Vertx HttpServerRequest
    // @param {boolean} secure Is the server SSL?
    // @param {Map} engines the current list of render engines (this is an unmodifiable map)
    // @return {YokeRequest} an Implementation of YokeRequest
    YokeRequest wrap(HttpServerRequest request, boolean secure, Map<String, Engine> engines);
}
