package com.jetdrone.vertx.yoke.store;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Set;

public class RedisSessionStore implements SessionStore {

    private final EventBus eventBus;

    private final Integer ttl;
    private final String prefix;
    private final String redisAddress;

    private static final int ONE_DAY = 86400;

    public RedisSessionStore(EventBus eventBus, String redisAddress, String prefix, Integer ttl) {
        this.redisAddress = redisAddress;
        this.prefix = prefix;
        this.ttl = ttl;
        this.eventBus = eventBus;
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

        if (this.ttl != null) {
            ttl = this.ttl;
        } else {
            if (maxAge != null) {
                ttl = maxAge / 1000;
            } else {
                ttl = ONE_DAY;
            }
        }

        JsonObject redis = new JsonObject();
        redis.putString("command", "setex");
        redis.putArray("args", new JsonArray().add(sid).add(ttl).add(session));

        eventBus.send(redisAddress, redis, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                if ("ok".equals(reply.body().getString("status"))) {
                    callback.handle("ok");
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
                    callback.handle("ok");
                } else {
                    callback.handle(reply.body().getString("message"));
                }
            }
        });
    }

    @Override
    public void all(Handler<JsonArray> callback) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clear(Handler<String> callback) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void length(Handler<Integer> callback) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
