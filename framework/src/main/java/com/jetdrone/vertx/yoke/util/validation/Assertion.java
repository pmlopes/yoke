package com.jetdrone.vertx.yoke.util.validation;

import com.jetdrone.vertx.yoke.core.YokeException;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;

public abstract class Assertion {

    public final int errorCode;

    public Assertion() {
        this(400);
    }

    public Assertion(int code) {
        this.errorCode = code;
    }

    public abstract void ok(YokeRequest request) throws YokeException;
}