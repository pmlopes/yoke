/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetdrone.vertx.yoke.extras.store;

import com.jetdrone.vertx.yoke.util.YokeAsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class MongoDbStore implements Store {

    final EventBus eb;
    final String address;

    public MongoDbStore(EventBus eb, String address) {
        this.eb = eb;
        this.address = address;
    }

    @Override
    public void read(String collection, String id, final AsyncResultHandler<JsonObject> handler) {
        JsonObject wrapper = new JsonObject();
        wrapper.putString("collection", collection);
        wrapper.putString("action", "findone");
        wrapper.putObject("matcher", new JsonObject().putString("_id", id));

        eb.send(address, wrapper, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                String status = reply.body().getString("status");

                if (status != null && "ok".equalsIgnoreCase(status)) {
                    JsonObject result = reply.body().getObject("result");

                    handler.handle(new YokeAsyncResult<>(null, result));
                } else {
                    handler.handle(new YokeAsyncResult<JsonObject>(new Throwable(status), null));
                }
            }
        });
    }

    @Override
    public void query(String collection, JsonObject query, Number start, Number end, JsonObject sort, final AsyncResultHandler<JsonArray> handler) {
        JsonObject wrapper = new JsonObject();
        wrapper.putString("collection", collection);
        wrapper.putString("action", "find");
        wrapper.putObject("matcher", query);

        if (start != null) {
            wrapper.putNumber("skip", start);
            if (end != null) {
                int limit = end.intValue() - start.intValue();
                wrapper.putNumber("limit", limit);
            }
        }

        if (sort != null) {
            wrapper.putObject("sort", sort);
        }

        final JsonArray result = new JsonArray();

        eb.send(address, wrapper, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                String status = reply.body().getString("status");

                if (status != null) {
                    if ("ok".equalsIgnoreCase(status)) {
                        JsonArray itResult = reply.body().getArray("results");
                        for (Object o : itResult) {
                            result.add(o);
                        }
                        handler.handle(new YokeAsyncResult<>(null, result));
                        return;
                    }
                    if ("more-exist".equalsIgnoreCase(status)) {
                        JsonArray itResult = reply.body().getArray("results");
                        for (Object o : itResult) {
                            result.add(o);
                        }
                        // reply asking for more
                        reply.reply(this);
                        return;
                    }
                }
                handler.handle(new YokeAsyncResult<JsonArray>(new Throwable(status), null));
            }
        });
    }

    @Override
    public void count(String collection, JsonObject query, final AsyncResultHandler<Number> handler) {
        JsonObject wrapper = new JsonObject();
        wrapper.putString("collection", collection);
        wrapper.putString("action", "count");
        wrapper.putObject("matcher", query);
        eb.send(address, wrapper, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                String status = reply.body().getString("status");

                if (status != null && "ok".equalsIgnoreCase(status)) {
                    Number result = reply.body().getNumber("count");

                    handler.handle(new YokeAsyncResult<>(null, result));
                } else {
                    handler.handle(new YokeAsyncResult<Number>(new Throwable(status), null));
                }
            }
        });
    }

    @Override
    public void delete(String collection, String id, final AsyncResultHandler<Number> handler) {
        JsonObject wrapper = new JsonObject();
        wrapper.putString("collection", collection);
        wrapper.putString("action", "delete");
        wrapper.putObject("matcher", new JsonObject().putString("_id", id));

        eb.send(address, wrapper, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                String status = reply.body().getString("status");

                if (status != null && "ok".equalsIgnoreCase(status)) {
                    Number result = reply.body().getNumber("number");

                    handler.handle(new YokeAsyncResult<>(null, result));
                } else {
                    handler.handle(new YokeAsyncResult<Number>(new Throwable(status), null));
                }
            }
        });
    }

    @Override
    public void create(String collection, JsonObject document, final AsyncResultHandler<String> handler) {
        JsonObject wrapper = new JsonObject();
        wrapper.putString("collection", collection);
        wrapper.putString("action", "save");
        wrapper.putObject("document", document);

        eb.send(address, wrapper, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                String status = reply.body().getString("status");

                if (status != null && "ok".equalsIgnoreCase(status)) {
                    String result = reply.body().getString("_id");
                    handler.handle(new YokeAsyncResult<>(null, result));
                } else {
                    handler.handle(new YokeAsyncResult<String>(new Throwable(status), null));
                }
            }
        });
    }

    @Override
    public void update(String collection, String id, JsonObject newDocument, final AsyncResultHandler<Number> handler) {
        JsonObject wrapper = new JsonObject();
        wrapper.putString("collection", collection);
        wrapper.putString("action", "update");
        wrapper.putObject("criteria", new JsonObject().putString("_id", id));

        wrapper.putObject("objNew", newDocument);
        eb.send(address, wrapper, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                String status = reply.body().getString("status");

                if (status != null && "ok".equalsIgnoreCase(status)) {
                    Number result = reply.body().getNumber("number");

                    handler.handle(new YokeAsyncResult<>(null, result));
                } else {
                    handler.handle(new YokeAsyncResult<Number>(new Throwable(status), null));
                }
            }
        });
    }
}
