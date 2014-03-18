package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.core.YokeException;

public interface Assert {

    public void ok(YokeRequest request) throws YokeException;
}
