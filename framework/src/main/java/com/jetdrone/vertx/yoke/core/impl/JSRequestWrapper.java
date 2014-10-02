package com.jetdrone.vertx.yoke.core.impl;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.core.Context;
import com.jetdrone.vertx.yoke.core.RequestWrapper;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.store.SessionStore;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.Map;

public class JSRequestWrapper implements RequestWrapper {

    @Override
    public YokeRequest wrap(HttpServerRequest request, Context context, Map<String, Engine> engines, SessionStore store) {
        return new JSYokeRequest(request, new JSYokeResponse(request.response(), context, engines), context, store);
    }
}
