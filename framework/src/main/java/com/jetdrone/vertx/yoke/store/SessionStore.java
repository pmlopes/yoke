/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.store;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/** # SessionStore */
public interface SessionStore {

    // Attempt to fetch session by the given `sid`.
    void get(String sid, Handler<JsonObject> callback);

    // Commit the given `sess` object associated with the given `sid`.
    void set(String sid, JsonObject sess, Handler<Object> callback);

    // Destroy the session associated with the given `sid`.
    void destroy(String sid, Handler<Object> callback);

    // Invoke the given callback `fn` with all active sessions.
    void all(Handler<JsonArray> callback);

    // Clear all sessions.
    void clear(Handler<Object> callback);

    // Fetch number of sessions.
    void length(Handler<Integer> callback);
}
