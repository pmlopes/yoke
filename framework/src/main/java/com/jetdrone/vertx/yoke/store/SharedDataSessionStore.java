/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.store;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.concurrent.ConcurrentMap;

/** #SharedDataSessionStore */
public class SharedDataSessionStore implements SessionStore {

    private final ConcurrentMap<String, String> storage;

    public SharedDataSessionStore(Vertx vertx, String name) {
        storage = vertx.sharedData().getMap(name);
    }

    @Override
    public void get(String sid, Handler<JsonObject> callback) {
        String sess = storage.get(sid);

        if (sess == null) {
            callback.handle(null);
            return;
        }

        callback.handle(new JsonObject(sess));
    }

    @Override
    public void set(String sid, JsonObject sess, Handler<Object> callback) {
        storage.put(sid, sess.encode());
        callback.handle(null);
    }

    @Override
    public void destroy(String sid, Handler<Object> callback) {
        storage.remove(sid);
        callback.handle(null);
    }

    @Override
    public void all(Handler<JsonArray> callback) {
        JsonArray items = new JsonArray();
        for (String s : storage.values()) {
            items.add(new JsonObject(s));
        }
        callback.handle(items);
    }

    @Override
    public void clear(Handler<Object> callback) {
        storage.clear();
        callback.handle(null);
    }

    @Override
    public void length(Handler<Integer> callback) {
        callback.handle(storage.size());
    }
}
