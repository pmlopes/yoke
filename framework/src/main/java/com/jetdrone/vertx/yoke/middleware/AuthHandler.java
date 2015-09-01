/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * # AuthHandler
 *
 * AuthHandler interface that needs to be implemented in order to validate usernames/passwords.
 */
public interface AuthHandler {
    /** Handles a challenge authentication request and asynchronously returns the user object on success, null for error.
     *
     * @param username the security principal user name
     * @param password the security principal password
     * @param  result authentication result
     */
    void handle(String username, String password, Handler<JsonObject> result);
}
