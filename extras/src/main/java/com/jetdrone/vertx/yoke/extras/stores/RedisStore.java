package com.jetdrone.vertx.yoke.extras.stores;

import com.jetdrone.vertx.yoke.util.YokeAsyncResult;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class RedisStore implements Store {

    final EventBus eb;
    final String address;
    final String prefix;

    public RedisStore(EventBus eb, String address, String prefix) {
        this.eb = eb;
        this.address = address;
        this.prefix = prefix;
    }

    private <T> void redis(final String command, final String key, final AsyncResultHandler<T> handler) {
        JsonObject keys = new JsonObject();
        keys.putString("command", command);
        keys.putString("key", key);
        eb.send(address, keys, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> msg) {
                if (!"ok".equals(msg.body().getString("status"))) {
                    handler.handle(new YokeAsyncResult<T>(new Throwable(msg.body().getString("message")), null));
                } else {
                    handler.handle(new YokeAsyncResult<>(null, (T) msg.body().getField("value")));
                }
            }
        });
    }

    private <T> void redis(final String command, final String key, final String value, final AsyncResultHandler<T> handler) {
        JsonObject keys = new JsonObject();
        keys.putString("command", command);
        keys.putString("key", key);
        keys.putString("value", value);
        eb.send(address, keys, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> msg) {
                if (!"ok".equals(msg.body().getString("status"))) {
                    handler.handle(new YokeAsyncResult<T>(new Throwable(msg.body().getString("message")), null));
                } else {
                    handler.handle(new YokeAsyncResult<>(null, (T) msg.body().getField("value")));
                }
            }
        });
    }

    @Override
    public void read(final String key, final AsyncResultHandler<JsonObject> handler) {
        redis("get", prefix + key, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> event) {
                if (event.failed()) {
                    handler.handle(new YokeAsyncResult<JsonObject>(event.cause(), null));
                    return;
                }

                // if not found (nil) return null to mark this as not found
                if (event.result() == null) {
                    handler.handle(new YokeAsyncResult<JsonObject>(null, null));
                } else {
                    handler.handle(new YokeAsyncResult<>(null, new JsonObject(event.result())));
                }
            }
        });
    }

    @Override
    public void create(final JsonObject value, final AsyncResultHandler<String> handler) {
        // generate Id
        redis("incr", prefix, new AsyncResultHandler<Number>() {
            @Override
            public void handle(AsyncResult<Number> event) {
                if (event.failed()) {
                    handler.handle(new YokeAsyncResult<String>(event.cause(), null));
                    return;
                }

                final String newId = event.result().toString();

                redis("setnx", prefix + newId, value.encode(), new AsyncResultHandler<Number>() {
                    @Override
                    public void handle(AsyncResult<Number> event) {
                        if (event.failed()) {
                            handler.handle(new YokeAsyncResult<String>(event.cause(), null));
                            return;
                        }

                        handler.handle(new YokeAsyncResult<>(null, newId));
                    }
                });
            }
        });
    }

    @Override
    public void delete(final String key, final AsyncResultHandler<Number> handler) {
        redis("del", prefix + key, handler);
    }

    public void keys(final AsyncResultHandler<JsonArray> handler) {
        JsonObject keys = new JsonObject();
        keys.putString("command", "keys");
        keys.putString("pattern", prefix + "*");
        eb.send(address, keys, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> msg) {
                if (!"ok".equals(msg.body().getString("status"))) {
                    handler.handle(new YokeAsyncResult<JsonArray>(new Throwable(msg.body().getString("message")), null));
                } else {
                    JsonArray redisKeys = msg.body().getArray("value");
                    handler.handle(new YokeAsyncResult<>(null, redisKeys));
                }
            }
        });
    }

    @Override
    public void update(final String key, JsonObject value, final AsyncResultHandler<Number> handler) {
        redis("set", prefix + key, value.encode(), handler);
    }

    @Override
    public void query(MultiMap query, AsyncResultHandler<JsonArray> response) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void count(MultiMap query, AsyncResultHandler<Long> response) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
