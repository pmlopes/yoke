package com.jetdrone.vertx.yoke.store;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import java.util.Set;

public interface SessionStore {

    // Attempt to fetch session by the given `sid`.
    void get(String sid, Handler<JsonObject> callback);

    // Commit the given `sess` object associated with the given `sid`.
    void set(String sid, JsonObject sess, Handler<Boolean> callback);

    // Destroy the session associated with the given `sid`.
    void destroy(String sid, Handler<Boolean> callback);

    // Invoke the given callback `fn` with all active sessions.
    void all(Handler<Set<JsonObject>> callback);

    // Clear all sessions.
    void clear(Handler<Boolean> callback);

    // Fetch number of sessions.
    void length(Handler<Integer> callback);
}
