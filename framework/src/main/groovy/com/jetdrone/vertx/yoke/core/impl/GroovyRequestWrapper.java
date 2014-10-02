package com.jetdrone.vertx.yoke.core.impl;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.core.Context;
import com.jetdrone.vertx.yoke.core.RequestWrapper;
import com.jetdrone.vertx.yoke.middleware.GYokeRequest;
import com.jetdrone.vertx.yoke.middleware.GYokeResponse;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.store.SessionStore;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.Map;

public class GroovyRequestWrapper implements RequestWrapper {

        @Override
        public YokeRequest wrap(HttpServerRequest request, Context context, Map<String, Engine> engines, SessionStore store) {
            return new GYokeRequest(request, new GYokeResponse(request.response(), context, engines), context, store);
        }
}
