/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.store;

import com.jetdrone.vertx.yoke.util.AsyncIterator;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/** # RedisSessionStore */
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
    public void set(String sid, JsonObject sess, final Handler<Object> callback) {
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
    public void destroy(String sid, final Handler<Object> callback) {
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
    public void all(final Handler<JsonArray> next) {
        JsonObject redis = new JsonObject();
        redis.putString("command", "keys");
        redis.putArray("args", new JsonArray().add(prefix + "*"));

        final JsonArray results = new JsonArray();

        eventBus.send(redisAddress, redis, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                if (!"ok".equals(message.body().getString("status"))) {
                    next.handle(null);
                } else {
                    JsonArray keys = message.body().getArray("value");

                    new AsyncIterator<Object>(keys.iterator()) {
                        @Override
                        public void handle(Object key) {
                            if (hasNext()) {
                                JsonObject redis = new JsonObject();
                                redis.putString("command", "get");
                                redis.putArray("args", new JsonArray().add(key));

                                eventBus.send(redisAddress, redis, new Handler<Message<JsonObject>>() {
                                    @Override
                                    public void handle(Message<JsonObject> message) {
                                        if (!"ok".equals(message.body().getString("status"))) {
                                            next.handle(null);
                                        } else {
                                            String value = message.body().getString("value");
                                            if (value != null) {
                                                results.add(new JsonObject(value));
                                            }
                                            next();
                                        }
                                    }
                                });
                            } else {
                                next.handle(results);
                            }
                        }
                    };
                }
            }
        });
    }

    @Override
    public void clear(final Handler<Object> next) {
        JsonObject redis = new JsonObject();
        redis.putString("command", "keys");
        redis.putArray("args", new JsonArray().add(prefix + "*"));

        eventBus.send(redisAddress, redis, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                if (!"ok".equals(message.body().getString("status"))) {
                    next.handle(message.body().getString("status"));
                } else {
                    JsonArray keys = message.body().getArray("value");

                    new AsyncIterator<Object>(keys.iterator()) {
                        @Override
                        public void handle(Object key) {
                            if (hasNext()) {
                                JsonObject redis = new JsonObject();
                                redis.putString("command", "del");
                                redis.putArray("args", new JsonArray().add(key));

                                eventBus.send(redisAddress, redis, new Handler<Message<JsonObject>>() {
                                    @Override
                                    public void handle(Message<JsonObject> message) {
                                        if (!"ok".equals(message.body().getString("status"))) {
                                            next.handle(message.body().getString("status"));
                                        } else {
                                            next();
                                        }
                                    }
                                });
                            } else {
                                next.handle(null);
                            }
                        }
                    };
                }
            }
        });
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

    @SuppressWarnings("unused")
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
