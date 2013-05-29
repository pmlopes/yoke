package com.jetdrone.vertx.yoke.extras.stores;

import com.jetdrone.vertx.yoke.util.YokeAsyncResult;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
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

    @Override
    public void read(final String key, final AsyncResultHandler<JsonObject> handler) {
        JsonObject keys = new JsonObject();
        keys.putString("command", "get");
        keys.putString("key", prefix + key);
        eb.send(address, keys, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> msg) {
                if (!"ok".equals(msg.body().getString("status"))) {
                    handler.handle(new YokeAsyncResult<JsonObject>(new Throwable(msg.body().getString("message")), null));
                } else {
                    handler.handle(new YokeAsyncResult<>(null, new JsonObject(msg.body().getString("value"))));
                }
            }
        });
    }

    private void generateId(final AsyncResultHandler<Number> handler) {
        JsonObject keys = new JsonObject();
        keys.putString("command", "incr");
        keys.putString("key", prefix + "seq");
        eb.send(address, keys, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> msg) {
                if (!"ok".equals(msg.body().getString("status"))) {
                    handler.handle(new YokeAsyncResult<Number>(new Throwable(msg.body().getString("message")), null));
                } else {
                    handler.handle(new YokeAsyncResult<>(null, msg.body().getNumber("value")));
                }
            }
        });
    }

    @Override
    public void create(final JsonObject value, final AsyncResultHandler<String> handler) {
        // generate Id
        generateId(new AsyncResultHandler<Number>() {
            @Override
            public void handle(final AsyncResult<Number> event) {
                if (event.failed()) {
                    handler.handle(new YokeAsyncResult<String>(event.cause(), null));
                    return;
                }

                JsonObject keys = new JsonObject();
                keys.putString("command", "setnx");
                keys.putString("key", prefix + event.result());
                keys.putString("value", value.encode());
                eb.send(address, keys, new Handler<Message<JsonObject>>() {
                    @Override
                    public void handle(Message<JsonObject> msg) {
                        if (!"ok".equals(msg.body().getString("status"))) {
                            handler.handle(new YokeAsyncResult<String>(new Throwable(msg.body().getString("message")), null));
                        } else {
                            handler.handle(new YokeAsyncResult<>(null, event.result().toString()));
                        }
                    }
                });
            }
        });
    }

    @Override
    public void delete(final String key, final AsyncResultHandler<Void> handler) {
        JsonObject keys = new JsonObject();
        keys.putString("command", "del");
        keys.putString("key", prefix + key);
        eb.send(address, keys, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> msg) {
                if (!"ok".equals(msg.body().getString("status"))) {
                    handler.handle(new YokeAsyncResult<Void>(new Throwable(msg.body().getString("message")), null));
                } else {
                    handler.handle(new YokeAsyncResult<Void>(null, null));
                }
            }
        });
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
    public void update(final String key, JsonObject value, final AsyncResultHandler<Void> handler) {
        JsonObject keys = new JsonObject();
        keys.putString("command", "set");
        keys.putString("key", prefix + key);
        keys.putString("value", value.encode());
        eb.send(address, keys, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> msg) {
                if (!"ok".equals(msg.body().getString("status"))) {
                    handler.handle(new YokeAsyncResult<Void>(new Throwable(msg.body().getString("message")), null));
                } else {
                    handler.handle(new YokeAsyncResult<Void>(null, null));
                }
            }
        });
    }

    @Override
    public void query(String query, AsyncResultHandler<JsonArray> response) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void count(String query, AsyncResultHandler<Long> response) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
