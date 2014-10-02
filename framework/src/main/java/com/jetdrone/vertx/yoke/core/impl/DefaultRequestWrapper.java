package com.jetdrone.vertx.yoke.core.impl;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.core.Context;
import com.jetdrone.vertx.yoke.core.RequestWrapper;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;
import com.jetdrone.vertx.yoke.store.SessionStore;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.Map;

public class DefaultRequestWrapper implements RequestWrapper {

    /**
     * Default implementation of the request wrapper
     */
    @Override
    public YokeRequest wrap(HttpServerRequest request, Context context, Map<String, Engine> engines, SessionStore store) {
        return new YokeRequest(request, new YokeResponse(request.response(), context, engines), context, store);
    }
}
