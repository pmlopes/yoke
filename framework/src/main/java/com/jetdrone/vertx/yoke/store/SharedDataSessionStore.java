package com.jetdrone.vertx.yoke.store;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

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
    public void set(String sid, JsonObject sess, Handler<Boolean> callback) {
        storage.put(sid, sess.encode());
        callback.handle(true);
    }

    @Override
    public void destroy(String sid, Handler<Boolean> callback) {
        storage.remove(sid);
        callback.handle(true);
    }

    @Override
    public void all(Handler<Set<JsonObject>> callback) {
        Set<JsonObject> items = new HashSet<>();
        for (String s : storage.values()) {
            items.add(new JsonObject(s));
        }
        callback.handle(items);
    }

    @Override
    public void clear(Handler<Boolean> callback) {
        storage.clear();
        callback.handle(true);
    }

    @Override
    public void length(Handler<Integer> callback) {
        callback.handle(storage.size());
    }
}
