/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.store;

import groovy.lang.Closure;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GSharedDataSessionStore extends SharedDataSessionStore {

    private static <T> Handler<T> wrapClosure(final Closure<T> closure) {
        return new Handler<T>() {
            @Override
            public void handle(T value) {
                    closure.call(value);
            }
        };
    }

    public GSharedDataSessionStore(Vertx vertx, String name) {
        super(vertx, name);
    }

    // Attempt to fetch session by the given `sid`.
    public void get(String sid, final Closure<Map> callback) {
        get(sid, new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject sess) {
                callback.call(sess == null ? null : sess.toMap());
            }
        });
    }

    // Commit the given `sess` object associated with the given `sid`.
    public void set(String sid, Map<String, Object> sess, Closure<Object> callback) {
        set(sid, new JsonObject(sess), wrapClosure(callback));
    }

    // Destroy the session associated with the given `sid`.
    public void destroy(String sid, Closure<Object> callback) {
        destroy(sid, wrapClosure(callback));
    }

    // Invoke the given callback `fn` with all active sessions.
    public void all(final Closure<List> callback) {
        all(new Handler<JsonArray>() {
            @Override
            public void handle(JsonArray sessions) {
                if (sessions == null) {
                    callback.call((List) null);
                    return;
                }
                List<Map<String, ?>> gSessions = new ArrayList<>();
                for (Object sess : sessions) {
                    JsonObject jsonSess = (JsonObject) sess;
                    gSessions.add(jsonSess.toMap());
                }

                callback.call(gSessions);
            }
        });
    }

    // Clear all sessions.
    public void clear(Closure<Object> callback) {
        clear(wrapClosure(callback));
    }

    // Fetch number of sessions.
    public void length(Closure<Integer> callback) {
        length(wrapClosure(callback));
    }
}
