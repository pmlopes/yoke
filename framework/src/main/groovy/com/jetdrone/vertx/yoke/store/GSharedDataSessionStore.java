package com.jetdrone.vertx.yoke.store;

import groovy.lang.Closure;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;

public class GSharedDataSessionStore extends SharedDataSessionStore {

    private static Handler wrapClosure(final Closure closure) {
        return new Handler() {
            @Override
            public void handle(Object value) {
                    closure.call(value);
            }
        };
    }

    public GSharedDataSessionStore(Vertx vertx, String name) {
        super(vertx, name);
    }

    // Attempt to fetch session by the given `sid`.
    public void get(String sid, Closure callback) {
        // TODO: convert back to Map
        get(sid, wrapClosure(callback));
    }

    // Commit the given `sess` object associated with the given `sid`.
    public void set(String sid, Map<String, Object> sess, Closure callback) {
        set(sid, new JsonObject(sess), wrapClosure(callback));
    }

    // Destroy the session associated with the given `sid`.
    public void destroy(String sid, Closure callback) {
        destroy(sid, wrapClosure(callback));
    }

    // Invoke the given callback `fn` with all active sessions.
    public void all(Closure callback) {
        // TODO: convert back to Map
        all(wrapClosure(callback));
    }

    // Clear all sessions.
    public void clear(Closure callback) {
        clear(wrapClosure(callback));
    }

    // Fetch number of sessions.
    public void length(Closure callback) {
        length(wrapClosure(callback));
    }
}
