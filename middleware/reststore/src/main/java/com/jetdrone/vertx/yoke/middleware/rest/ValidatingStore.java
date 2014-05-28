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
package com.jetdrone.vertx.yoke.middleware.rest;

import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public abstract class ValidatingStore implements Store {

    private final Store baseStore;

    public ValidatingStore(Store store) {
        this.baseStore = store;
    }

    private static final YokeAsyncResult<String> OK_STRING = new YokeAsyncResult<>(null, null);
    private static final YokeAsyncResult<JsonObject> OK_JSONOBJECT = new YokeAsyncResult<>(null, null);
    private static final YokeAsyncResult<JsonArray> OK_JSONARRAY = new YokeAsyncResult<>(null, null);
    private static final YokeAsyncResult<Number> OK_NUMBER = new YokeAsyncResult<>(null, null);

    public void beforeCreate(String entity, JsonObject object, AsyncResultHandler<String> response) {
        response.handle(OK_STRING);
    }

    public void afterCreate(String entity, JsonObject object, AsyncResultHandler<String> response) {
        response.handle(OK_STRING);
    }

    @Override
    public final void create(final String entity, final JsonObject object, final AsyncResultHandler<String> response) {
        beforeCreate(entity, object, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> event) {
                if (event.failed()) {
                    response.handle(event);
                    return;
                }

                baseStore.create(entity, object, new AsyncResultHandler<String>() {
                    @Override
                    public void handle(AsyncResult<String> event) {
                        if (event.failed()) {
                            response.handle(event);
                            return;
                        }

                        afterCreate(entity, object, new AsyncResultHandler<String>() {
                            @Override
                            public void handle(AsyncResult<String> event) {
                                if (event.failed()) {
                                    response.handle(event);
                                    return;
                                }

                                response.handle(event);
                            }
                        });
                    }
                });
            }
        });
    }

    public void beforeRead(String entity, String id, AsyncResultHandler<JsonObject> response) {
        response.handle(OK_JSONOBJECT);
    }

    public void afterRead(String entity, String id, AsyncResultHandler<JsonObject> response) {
        response.handle(OK_JSONOBJECT);
    }

    @Override
    public final void read(final String entity, final String id, final AsyncResultHandler<JsonObject> response) {
        beforeRead(entity, id, new AsyncResultHandler<JsonObject>() {
            @Override
            public void handle(AsyncResult<JsonObject> event) {
                if (event.failed()) {
                    response.handle(event);
                    return;
                }

                baseStore.read(entity, id, new AsyncResultHandler<JsonObject>() {
                    @Override
                    public void handle(AsyncResult<JsonObject> event) {
                        if (event.failed()) {
                            response.handle(event);
                            return;
                        }

                        afterRead(entity, id, new AsyncResultHandler<JsonObject>() {
                            @Override
                            public void handle(AsyncResult<JsonObject> event) {
                                if (event.failed()) {
                                    response.handle(event);
                                    return;
                                }

                                response.handle(event);
                            }
                        });
                    }
                });
            }
        });
    }

    public void beforeUpdate(String entity, String id, JsonObject object, AsyncResultHandler<Number> response) {
        response.handle(OK_NUMBER);
    }

    public void afterUpdate(String entity, String id, JsonObject object, AsyncResultHandler<Number> response) {
        response.handle(OK_NUMBER);
    }

    @Override
    public final void update(final String entity, final String id, final JsonObject object, final AsyncResultHandler<Number> response) {
        beforeUpdate(entity, id, object, new AsyncResultHandler<Number>() {
            @Override
            public void handle(AsyncResult<Number> event) {
                if (event.failed()) {
                    response.handle(event);
                    return;
                }

                baseStore.update(entity, id, object, new AsyncResultHandler<Number>() {
                    @Override
                    public void handle(AsyncResult<Number> event) {
                        if (event.failed()) {
                            response.handle(event);
                            return;
                        }

                        afterUpdate(entity, id, object, new AsyncResultHandler<Number>() {
                            @Override
                            public void handle(AsyncResult<Number> event) {
                                if (event.failed()) {
                                    response.handle(event);
                                    return;
                                }

                                response.handle(event);
                            }
                        });
                    }
                });
            }
        });
    }

    public void beforeDelete(String entity, String id, AsyncResultHandler<Number> response) {
        response.handle(OK_NUMBER);
    }

    public void afterDelete(String entity, String id, AsyncResultHandler<Number> response) {
        response.handle(OK_NUMBER);
    }

    @Override
    public final void delete(final String entity, final String id, final AsyncResultHandler<Number> response) {
        beforeDelete(entity, id, new AsyncResultHandler<Number>() {
            @Override
            public void handle(AsyncResult<Number> event) {
                if (event.failed()) {
                    response.handle(event);
                    return;
                }

                baseStore.delete(entity, id, new AsyncResultHandler<Number>() {
                    @Override
                    public void handle(AsyncResult<Number> event) {
                        if (event.failed()) {
                            response.handle(event);
                            return;
                        }

                        afterDelete(entity, id, new AsyncResultHandler<Number>() {
                            @Override
                            public void handle(AsyncResult<Number> event) {
                                if (event.failed()) {
                                    response.handle(event);
                                    return;
                                }

                                response.handle(event);
                            }
                        });
                    }
                });
            }
        });
    }

    public void beforeQuery(String entity, JsonObject query, Number start, Number end, JsonObject sort, AsyncResultHandler<JsonArray> response) {
        response.handle(OK_JSONARRAY);
    }

    public void afterQuery(String entity, JsonObject query, Number start, Number end, JsonObject sort, AsyncResultHandler<JsonArray> response) {
        response.handle(OK_JSONARRAY);
    }

    @Override
    public final void query(final String entity, final JsonObject query, final Number start, final Number end, final JsonObject sort, final AsyncResultHandler<JsonArray> response) {
        beforeQuery(entity, query, start, end, sort, new AsyncResultHandler<JsonArray>() {
            @Override
            public void handle(AsyncResult<JsonArray> event) {
                if (event.failed()) {
                    response.handle(event);
                    return;
                }

                baseStore.query(entity, query, start, end, sort, new AsyncResultHandler<JsonArray>() {
                    @Override
                    public void handle(AsyncResult<JsonArray> event) {
                        if (event.failed()) {
                            response.handle(event);
                            return;
                        }

                        afterQuery(entity, query, start, end, sort, new AsyncResultHandler<JsonArray>() {
                            @Override
                            public void handle(AsyncResult<JsonArray> event) {
                                if (event.failed()) {
                                    response.handle(event);
                                    return;
                                }

                                response.handle(event);
                            }
                        });
                    }
                });
            }
        });
    }

    public void beforeCount(String entity, JsonObject query, AsyncResultHandler<Number> response) {
        response.handle(OK_NUMBER);
    }

    public void afterCount(String entity, JsonObject query, AsyncResultHandler<Number> response) {
        response.handle(OK_NUMBER);
    }

    @Override
    public final void count(final String entity, final JsonObject query, final AsyncResultHandler<Number> response) {
        beforeCount(entity, query, new AsyncResultHandler<Number>() {
            @Override
            public void handle(AsyncResult<Number> event) {
                if (event.failed()) {
                    response.handle(event);
                    return;
                }

                baseStore.count(entity, query, new AsyncResultHandler<Number>() {
                    @Override
                    public void handle(AsyncResult<Number> event) {
                        if (event.failed()) {
                            response.handle(event);
                            return;
                        }

                        afterCount(entity, query, new AsyncResultHandler<Number>() {
                            @Override
                            public void handle(AsyncResult<Number> event) {
                                if (event.failed()) {
                                    response.handle(event);
                                    return;
                                }

                                response.handle(event);
                            }
                        });
                    }
                });
            }
        });
    }
}
