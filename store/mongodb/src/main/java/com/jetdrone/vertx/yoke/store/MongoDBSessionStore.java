/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.store;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/** # MongoDBSessionStore
 *
 * mongo db collection *MUST* have a TTL index on updatedAt
 *
 *     db.collection.ensureIndex({updatedAt: 1}, {expireAfterSeconds: 3600});
 */
public class MongoDBSessionStore implements SessionStore {

    private final EventBus eventBus;

    private final String collection;
    private final String mongoAddress;

    public MongoDBSessionStore(EventBus eventBus, String mongoAddress, String collection) {
        this.mongoAddress = mongoAddress;
        this.collection = collection;
        this.eventBus = eventBus;
    }

    @Override
    public void get(final String sid, final Handler<JsonObject> next) {
        JsonObject mongo = new JsonObject()
                .putString("action", "findone")
                .putString("collection", collection)
                .putObject("matcher", new JsonObject().putString("id", sid));

        eventBus.send(mongoAddress, mongo, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                if ("ok".equals(reply.body().getString("status"))) {
                    JsonObject value = reply.body().getObject("result");
                    if (value == null) {
                        next.handle(null);
                        return;
                    }

                    // clean mongodb specific fields
                    value.removeField("_id");
                    value.removeField("updatedAt");

                    next.handle(value);
                } else {
                    next.handle(null);
                }
            }
        });
    }

    @Override
    public void set(final String sid, JsonObject sess, final Handler<Object> next) {
        // force the session id
        sess.putString("id", sid);
        // updated at
        sess.putObject("updatedAt", new JsonObject().putNumber("$date", System.currentTimeMillis()));

        JsonObject mongo = new JsonObject()
                .putString("action", "update")
                .putString("collection", collection)
                .putObject("criteria", new JsonObject().putString("id", sid))
                .putObject("objNew", sess)
                .putBoolean("upsert", true)
                .putBoolean("multi", false);

        eventBus.send(mongoAddress, mongo, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                if ("ok".equals(reply.body().getString("status"))) {
                    next.handle(null);
                } else {
                    next.handle(reply.body().getString("message"));
                }
            }
        });
    }

    @Override
    public void destroy(String sid, final Handler<Object> next) {
        JsonObject mongo = new JsonObject()
                .putString("action", "delete")
                .putString("collection", collection)
                .putObject("matcher", new JsonObject().putString("id", sid));

        eventBus.send(mongoAddress, mongo, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                if ("ok".equals(reply.body().getString("status"))) {
                    next.handle(null);
                } else {
                    next.handle(reply.body().getString("message"));
                }
            }
        });
    }

    @Override
    public void all(final Handler<JsonArray> next) {
        JsonObject wrapper = new JsonObject();
        wrapper.putString("collection", collection);
        wrapper.putString("action", "find");
        wrapper.putObject("matcher", new JsonObject());

        final JsonArray result = new JsonArray();

        eventBus.send(mongoAddress, wrapper, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                String status = reply.body().getString("status");

                if (status != null) {
                    if ("ok".equalsIgnoreCase(status)) {
                        JsonArray itResult = reply.body().getArray("results");
                        for (Object o : itResult) {
                            JsonObject json = (JsonObject) o;

                            // clean mongodb specific fields
                            json.removeField("_id");
                            json.removeField("updatedAt");

                            result.add(json);
                        }
                        next.handle(result);
                        return;
                    }
                    if ("more-exist".equalsIgnoreCase(status)) {
                        JsonArray itResult = reply.body().getArray("results");
                        for (Object o : itResult) {
                            JsonObject json = (JsonObject) o;

                            // clean mongodb specific fields
                            json.removeField("_id");
                            json.removeField("updatedAt");

                            result.add(json);
                        }
                        // reply asking for more
                        reply.reply(this);
                        return;
                    }
                }

                next.handle(null);
            }
        });
    }

    @Override
    public void clear(final Handler<Object> next) {
        JsonObject mongo = new JsonObject()
                .putString("action", "delete")
                .putString("collection", collection)
                .putObject("matcher", new JsonObject());

        eventBus.send(mongoAddress, mongo, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                if ("ok".equals(reply.body().getString("status"))) {
                    next.handle(null);
                } else {
                    next.handle(reply.body().getString("message"));
                }
            }
        });
    }

    @Override
    public void length(final Handler<Integer> next) {
        JsonObject mongo = new JsonObject();
        mongo.putString("action", "count");
        mongo.putString("collection", collection);

        eventBus.send(mongoAddress, mongo, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                if (!"ok".equals(message.body().getString("status"))) {
                    next.handle(0);
                } else {
                    next.handle(message.body().getInteger("count"));
                }
            }
        });
    }
}
