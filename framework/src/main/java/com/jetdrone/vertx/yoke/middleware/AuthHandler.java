package com.jetdrone.vertx.yoke.middleware;

import org.vertx.java.core.Handler;

// ## AuthHandler
// AuthHandler interface that needs to be implemented in order to validate usernames/passwords.
public interface AuthHandler {
    // Handles a challenge authentication request and asynchronously returns true on success.
    //
    // @method handle
    // @asynchronous
    //
    // @param {String} username
    // @param {String} password
    // @param {Handler} result
    void handle(String username, String password, Handler<Boolean> result);
}
