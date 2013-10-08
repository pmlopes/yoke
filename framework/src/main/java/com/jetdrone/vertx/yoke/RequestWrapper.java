// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke
package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.Map;

public interface RequestWrapper {

    // For other language bindings this method can be override.
    //
    // @method wrap
    // @param {HttpServerRequest} request The Vertx HttpServerRequest
    // @param {boolean} secure Is the server SSL?
    // @param {Map} context The shared context between request and response
    // @param {Map} engines the current list of render engines (this is an unmodifiable map)
    // @return {YokeRequest} an Implementation of YokeRequest
    YokeRequest wrap(HttpServerRequest request, boolean secure, Map<String, Object> context, Map<String, Engine> engines);
}
