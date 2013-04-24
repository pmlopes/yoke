package com.jetdrone.vertx.kitcms;

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class Db {

    final EventBus eb;
    final String address;

    public Db(EventBus eb, String address) {
        this.eb = eb;
        this.address = address;
    }

    public void get(final String namespace, final String key, final AsyncResultHandler<String> handler) {
        JsonObject keys = new JsonObject();
        keys.putString("command", "get");
        keys.putString("key", namespace + "&" + key);
        eb.send(address, keys, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> msg) {
                if (!"ok".equals(msg.body().getString("status"))) {
                    handler.handle(new FutureAsyncResult<String>(new Throwable(msg.body().getString("message")), null));
                } else {
                    handler.handle(new FutureAsyncResult<>(null, msg.body().getString("value")));
                }
            }
        });
    }

    public void set(final String namespace, final String key, final String value, final AsyncResultHandler<Void> handler) {
        JsonObject keys = new JsonObject();
        keys.putString("command", "set");
        keys.putString("key", namespace + "&" + key);
        keys.putString("value", value);
        eb.send(address, keys, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> msg) {
                if (!"ok".equals(msg.body().getString("status"))) {
                    handler.handle(new FutureAsyncResult<Void>(new Throwable(msg.body().getString("message")), null));
                } else {
                    handler.handle(new FutureAsyncResult<Void>(null, null));
                }
            }
        });
    }

    public void unset(final String namespace, final String key, final AsyncResultHandler<Void> handler) {
        JsonObject keys = new JsonObject();
        keys.putString("command", "del");
        keys.putString("key", namespace + "&" + key);
        eb.send(address, keys, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> msg) {
                if (!"ok".equals(msg.body().getString("status"))) {
                    handler.handle(new FutureAsyncResult<Void>(new Throwable(msg.body().getString("message")), null));
                } else {
                    handler.handle(new FutureAsyncResult<Void>(null, null));
                }
            }
        });
    }

    public void keys(final String namespace, final AsyncResultHandler<JsonArray> handler) {
        JsonObject keys = new JsonObject();
        keys.putString("command", "keys");
        keys.putString("pattern", namespace + "&*");
        eb.send(address, keys, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> msg) {
                if (!"ok".equals(msg.body().getString("status"))) {
                    handler.handle(new FutureAsyncResult<JsonArray>(new Throwable(msg.body().getString("message")), null));
                } else {
                    JsonArray redisKeys = msg.body().getArray("value");
                    JsonArray keys = new JsonArray();
                    int len = namespace.length() + 1;
                    // Remove namespace from keys
                    for (int i = 0; i < redisKeys.size(); i++) {
                        String key = redisKeys.get(i);
                        keys.add(key.substring(len));
                    }
                    handler.handle(new FutureAsyncResult<>(null, keys));
                }
            }
        });
    }
}
