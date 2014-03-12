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
package com.jetdrone.vertx.yoke.extras.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.Router;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.extras.store.Store;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonRestRouter extends Router {

    // GET /
    public static final int QUERY =     1;
    // GET /:id
    public static final int READ =      2;
    // PUT /:id
    public static final int UPDATE =    4;
    // PATCH /:id
    public static final int APPEND =    8;
    // POST /
    public static final int CREATE =    16;
    // DELETE /:id
    public static final int DELETE =    32;

    private final String sortParam;
    private final Pattern sortPattern = Pattern.compile("sort\\((.+)\\)");

    private final Store store;

    private static final Middleware NOT_ALLOWED = new Middleware() {
        @Override
        public void handle(YokeRequest request, Handler<Object> next) {
            next.handle(405);
        }
    };

    public JsonRestRouter(Store store) {
        this.store = store;
        this.sortParam = null;
    }

    private boolean isAllowed(int operation, int allowedOperations) {
        return (allowedOperations & operation) == operation;
    }


    public JsonRestRouter rest(String resource, String entity) {
        return rest(resource, entity, QUERY + READ + UPDATE + APPEND + CREATE + DELETE);
    }

    public JsonRestRouter rest(String resource, String entity, int allowedOperations) {
        // build the resource url
        String resourcePath = entity.endsWith("/") ? resource.substring(0, resource.length() - 1) : resource;

        if (isAllowed(QUERY, allowedOperations)) {
            super.get(resourcePath, query(entity));
        } else {
            super.get(resourcePath, NOT_ALLOWED);
        }

        if (isAllowed(READ, allowedOperations)) {
            super.get(resourcePath + "/:" + entity, read(entity));
        } else {
            super.get(resourcePath + "/:" + entity, NOT_ALLOWED);
        }

        if (isAllowed(UPDATE, allowedOperations)) {
            super.put(resourcePath + "/:" + entity, update(entity));
        } else {
            super.put(resourcePath + "/:" + entity, NOT_ALLOWED);
        }

        if (isAllowed(APPEND, allowedOperations)) {
            // shortcut for patch (as by Dojo Toolkit)
            super.post(resourcePath + "/:" + entity, append(entity));
            super.patch(resourcePath + "/:" + entity, append(entity));
        } else {
            // shortcut for patch (as by Dojo Toolkit)
            super.post(resourcePath + "/:" + entity, NOT_ALLOWED);
            super.patch(resourcePath + "/:" + entity, NOT_ALLOWED);
        }

        if (isAllowed(CREATE, allowedOperations)) {
            super.post(resourcePath, create(entity));
        } else {
            super.post(resourcePath, NOT_ALLOWED);
        }

        if (isAllowed(DELETE, allowedOperations)) {
            super.delete(resourcePath + "/:" + entity, delete(entity));
        } else {
            super.delete(resourcePath + "/:" + entity, NOT_ALLOWED);
        }

        return this;
    }

    private Middleware delete(final String idName) {
        return new Middleware() {
            @Override
            public void handle(final YokeRequest request, final Handler<Object> next) {
                // get the real id from the params multimap
                final String id = request.params().get(idName);

                store.delete(idName, id, new AsyncResultHandler<Number>() {
                    @Override
                    public void handle(AsyncResult<Number> event) {
                        if (event.failed()) {
                            next.handle(event.cause());
                            return;
                        }

                        if (event.result().intValue() == 0) {
                            request.response().setStatusCode(404);
                            request.response().end();
                        } else {
                            request.response().setStatusCode(204);
                            request.response().end();
                        }
                    }
                });
            }
        };
    }

    private Middleware create(final String idName) {
        return new Middleware() {
            @Override
            public void handle(final YokeRequest request, final Handler<Object> next) {
                JsonObject item = request.body();

                if (item == null) {
                    next.handle("Body must be JSON");
                    return;
                }

                store.create(idName, item, new AsyncResultHandler<String>() {
                    @Override
                    public void handle(AsyncResult<String> event) {
                        if (event.failed()) {
                            next.handle(event.cause());
                            return;
                        }
                        request.response().putHeader("location", request.path() + "/" + event.result());
                        request.response().setStatusCode(201);
                        request.response().end();
                    }
                });
            }
        };
    }

    private Middleware append(final String idName) {
        return new Middleware() {
            @Override
            public void handle(final YokeRequest request, final Handler<Object> next) {
                // get the real id from the params multimap
                final String id = request.params().get(idName);

                store.read(idName, id, new AsyncResultHandler<JsonObject>() {
                    @Override
                    public void handle(AsyncResult<JsonObject> event) {
                        if (event.failed()) {
                            next.handle(event.cause());
                            return;
                        }

                        if (event.result() == null) {
                            // does not exist, returns 404
                            request.response().setStatusCode(404);
                            request.response().end();
                        } else {
                            // merge existing json with incoming one
                            Boolean overwrite = null;

                            if ("*".equals(request.getHeader("if-match"))) {
                                overwrite = true;
                            }

                            if ("*".equals(request.getHeader("if-none-match"))) {
                                overwrite = false;
                            }

                            // TODO: handle overwrite
                            final JsonObject obj = event.result();
                            obj.mergeIn((JsonObject) request.body());

                            // update back to the db
                            store.update(idName, id, obj, new AsyncResultHandler<Number>() {
                                @Override
                                public void handle(AsyncResult<Number> event) {
                                    if (event.failed()) {
                                        next.handle(event.cause());
                                        return;
                                    }

                                    if (event.result().intValue() == 0) {
                                        // nothing was updated
                                        request.response().setStatusCode(404);
                                        request.response().end();
                                    } else {
                                        request.response().setStatusCode(204);
                                        request.response().end();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        };
    }

    private Middleware update(final String idName) {
        return new Middleware() {
            @Override
            public void handle(final YokeRequest request, final Handler<Object> next) {
                JsonObject item = request.body();

                if (item == null) {
                    next.handle("Body must be JSON");
                    return;
                }

                // get the real id from the params multimap
                String id = request.params().get(idName);

                store.update(idName, id, item, new AsyncResultHandler<Number>() {
                    @Override
                    public void handle(AsyncResult<Number> event) {
                        if (event.failed()) {
                            next.handle(event.cause());
                            return;
                        }

                        if (event.result().intValue() == 0) {
                            // nothing was updated
                            request.response().setStatusCode(404);
                            request.response().end();
                        } else {
                            request.response().setStatusCode(204);
                            request.response().end();
                        }
                    }
                });
            }
        };
    }

    private static Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    private Middleware query(final String idName) {
        // range pattern
        final Pattern rangePattern = Pattern.compile("items=(\\d+)-(\\d+)");

        return new Middleware() {
            @Override
            public void handle(final YokeRequest request, final Handler<Object> next) {
                // content negotiation
                if (request.accepts("application/json") == null) {
                    // Not Acceptable (we only talk json)
                    next.handle(406);
                    return;
                }

                // parse ranges
                final String range = request.getHeader("range");
                final Integer start, end;
                if (range != null) {
                    Matcher m = rangePattern.matcher(range);
                    if (m.matches()) {
                        start = parseInt(m.group(1));
                        end = parseInt(m.group(2));
                    } else {
                        start = null;
                        end = null;
                    }
                } else {
                    start = null;
                    end = null;
                }

                // parse query
                final JsonObject dbquery = new JsonObject();
                final JsonObject dbsort = new JsonObject();
                for (Map.Entry<String, String> entry : request.params()) {
                    String[] sortArgs;
                    // parse sort
                    if (sortParam == null) {
                        Matcher sort = sortPattern.matcher(entry.getKey());

                        if (sort.matches()) {
                            sortArgs = sort.group(1).split(",");
                            for (String arg : sortArgs) {
                                if (arg.charAt(0) == '+' || arg.charAt(0) == ' ') {
                                    dbsort.putNumber(arg.substring(1), 1);
                                } else if (arg.charAt(0) == '-') {
                                    dbsort.putNumber(arg.substring(1), -1);
                                }
                            }
                            continue;
                        }
                    } else {
                        if (sortParam.equals(entry.getKey())) {
                            sortArgs = entry.getValue().split(",");
                            for (String arg : sortArgs) {
                                if (arg.charAt(0) == '+' || arg.charAt(0) == ' ') {
                                    dbsort.putNumber(arg.substring(1), 1);
                                } else if (arg.charAt(0) == '-') {
                                    dbsort.putNumber(arg.substring(1), -1);
                                }
                            }
                            continue;
                        }
                    }
                    dbquery.putString(entry.getKey(), entry.getValue());
                }

                store.query(idName, dbquery, start, end, dbsort, new AsyncResultHandler<JsonArray>() {
                    @Override
                    public void handle(final AsyncResult<JsonArray> query) {
                        if (query.failed()) {
                            next.handle(query.cause());
                            return;
                        }

                        if (range != null) {
                            // need to send the content-range with totals
                            store.count(idName, dbquery, new AsyncResultHandler<Number>() {
                                @Override
                                public void handle(AsyncResult<Number> count) {
                                    if (count.failed()) {
                                        next.handle(count.cause());
                                        return;
                                    }

                                    Integer realEnd = end;

                                    if (start != null && end != null) {
                                        realEnd = start + query.result().size();
                                    }

                                    request.response().putHeader("content-range", "items " + start + "-" + realEnd + "/" + count.result());
                                    request.response().end(query.result());
                                }
                            });
                            return;
                        }

                        request.response().end(query.result());
                    }
                });
            }
        };
    }

    private Middleware read(final String idName) {
        return new Middleware() {
            @Override
            public void handle(final YokeRequest request, final Handler<Object> next) {
                // content negotiation
                if (request.accepts("application/json") == null) {
                    // Not Acceptable (we only talk json)
                    next.handle(406);
                    return;
                }

                // get the real id from the params multimap
                String id = request.params().get(idName);

                store.read(idName, id, new AsyncResultHandler<JsonObject>() {
                    @Override
                    public void handle(AsyncResult<JsonObject> event) {
                        if (event.failed()) {
                            next.handle(event.cause());
                            return;
                        }

                        if (event.result() == null) {
                            // does not exist, returns 404
                            request.response().setStatusCode(404);
                            request.response().end();
                        } else {
                            request.response().end(event.result());
                        }
                    }
                });
            }
        };
    }
}
