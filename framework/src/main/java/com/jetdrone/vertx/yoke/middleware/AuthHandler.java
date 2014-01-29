/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

// ## AuthHandler
// AuthHandler interface that needs to be implemented in order to validate usernames/passwords.
public interface AuthHandler {
    // Handles a challenge authentication request and asynchronously returns the user object on success, null for error.
    //
    // @method handle
    // @asynchronous
    //
    // @param {String} username
    // @param {String} password
    // @param {Handler} result
    void handle(String username, String password, Handler<JsonObject> result);
}
