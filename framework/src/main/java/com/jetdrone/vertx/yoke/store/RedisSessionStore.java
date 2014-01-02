package com.jetdrone.vertx.yoke.store;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Iterator;

public class RedisSessionStore implements SessionStore {

    private final EventBus eventBus;

    private final int ttl;
    private final String prefix;
    private final String redisAddress;

    public RedisSessionStore(EventBus eventBus, String redisAddress, String prefix, Integer ttl) {
        this.redisAddress = redisAddress;
        this.prefix = prefix;
        this.ttl = ttl;
        this.eventBus = eventBus;
    }

    public RedisSessionStore(EventBus eventBus, String redisAddress, String prefix) {
        this(eventBus, redisAddress, prefix, 86400);
    }

    @Override
    public void get(String sid, final Handler<JsonObject> callback) {
        sid = this.prefix + sid;

        JsonObject redis = new JsonObject();
        redis.putString("command", "get");
        redis.putArray("args", new JsonArray().add(sid));

        eventBus.send(redisAddress, redis, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                if ("ok".equals(reply.body().getString("status"))) {
                    String value = reply.body().getString("value");
                    if (value == null || "".equals(value)) {
                        callback.handle(null);
                        return;
                    }
                    callback.handle(new JsonObject(value));
                } else {
                    callback.handle(null);
                }
            }
        });
    }

    @Override
    public void set(String sid, JsonObject sess, final Handler<String> callback) {
        sid = prefix + sid;

        Integer maxAge = null;

        JsonObject obj = sess.getObject("cookie");
        if (obj != null) {
            maxAge = obj.getInteger("maxAge");
        }

        String session = sess.encode();
        int ttl;

        if (maxAge != null) {
            ttl = maxAge / 1000;
        } else {
            ttl = this.ttl;
        }

        JsonObject redis = new JsonObject();
        redis.putString("command", "setex");
        redis.putArray("args", new JsonArray().add(sid).add(ttl).add(session));

        eventBus.send(redisAddress, redis, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                if ("ok".equals(reply.body().getString("status"))) {
                    callback.handle(null);
                } else {
                    callback.handle(reply.body().getString("message"));
                }
            }
        });
    }

    @Override
    public void destroy(String sid, final Handler<String> callback) {
        sid = this.prefix + sid;

        JsonObject redis = new JsonObject();
        redis.putString("command", "del");
        redis.putArray("args", new JsonArray().add(sid));

        eventBus.send(redisAddress, redis, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                if ("ok".equals(reply.body().getString("status"))) {
                    callback.handle(null);
                } else {
                    callback.handle(reply.body().getString("message"));
                }
            }
        });
    }

    @Override
    public void all(Handler<JsonArray> callback) {
        // REDIS keys prefix *
        // for each fetch
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void clear(Handler<String> callback) {
        // REDIS keys prefix *
        // for each DEL
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void length(final Handler<Integer> next) {
        JsonObject redis = new JsonObject();
        redis.putString("command", "keys");
        redis.putArray("args", new JsonArray().add(prefix + "*"));

        eventBus.send(redisAddress, redis, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                if (!"ok".equals(message.body().getString("status"))) {
                    next.handle(0);
                } else {
                    JsonArray keys = message.body().getArray("value");
                    next.handle(keys.size());
                }
            }
        });
    }


    private void getKeys(final Handler<JsonArray> next) {
        JsonObject redis = new JsonObject();
        redis.putString("command", "keys");
        redis.putArray("args", new JsonArray().add(prefix + "*"));

        eventBus.send(redisAddress, redis, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                if (!"ok".equals(message.body().getString("status"))) {
                    next.handle(new JsonArray());
                } else {
                    JsonArray keys = message.body().getArray("value");

                    JsonArray result = new JsonArray();
                    int len = prefix.length();

                    for (Object o : keys) {
                        String key = (String) o;
                        result.add(key.substring(len));
                    }

                    next.handle(result);
                }
            }
        });
    }
}
