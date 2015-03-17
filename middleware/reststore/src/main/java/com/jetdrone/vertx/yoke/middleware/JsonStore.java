package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.IMiddleware;
import com.jetdrone.vertx.yoke.Middleware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonStore extends Router {

    private static final Pattern RANGE = Pattern.compile("items=(\\d+)-(\\d+)");
    private static final Pattern SORT = Pattern.compile("sort\\((.+)\\)");

    private final String prefix;
    private final String sortParam;

    public JsonStore() {
        this("/api");
    }

    public JsonStore(String prefix) {
        this(prefix, null);
    }

    public JsonStore(String prefix, String sortParam) {
        this.prefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        this.sortParam = sortParam;
    }

    protected CRUD createCrud() {
        return new CRUD();
    }

    public CRUD collection(String name) {
        return collection(name, "id");
    }

    public CRUD collection(String name, @Nullable final IMiddleware validator) {
        return collection(name, "id", validator);
    }

    public CRUD collection(String name, String key) {
        return collection(name, key, null);
    }

    public CRUD collection(@NotNull final String name, @NotNull final String key, @Nullable final IMiddleware validator) {
        final CRUD crud = createCrud();
        collection(name, key, crud, validator);
        return crud;
    }

    public void collection(@NotNull final String name, @NotNull final String key, @NotNull final CRUD crud, @Nullable final IMiddleware validator) {

        if (validator != null) {
            // CREATE
            post(prefix + "/" + name, validator, create(name, key, crud));
            // READ ALL
            get(prefix + "/" + name, validator, query(name, key, crud));
            // READ ONE
            get(prefix + "/" + name + "/:" + name, validator, read(name, key, crud));
            // UPDATE
            put(prefix + "/" + name + "/:" + name, validator, update(name, key, crud));
            // DELETE
            delete(prefix + "/" + name + "/:" + name, validator, delete(name, key, crud));

            // shortcut for patch (as by Dojo Toolkit)
            post(prefix + "/" + name + "/:" + name, validator, append(name, key, crud));
            patch(prefix + "/" + name + "/:" + name, validator, append(name, key, crud));
        } else {
            // CREATE
            post(prefix + "/" + name, create(name, key, crud));
            // READ ALL
            get(prefix + "/" + name, query(name, key, crud));
            // READ ONE
            get(prefix + "/" + name + "/:" + name, read(name, key, crud));
            // UPDATE
            put(prefix + "/" + name + "/:" + name, update(name, key, crud));
            // DELETE
            delete(prefix + "/" + name + "/:" + name, delete(name, key, crud));

            // shortcut for patch (as by Dojo Toolkit)
            post(prefix + "/" + name + "/:" + name, append(name, key, crud));
            patch(prefix + "/" + name + "/:" + name, append(name, key, crud));
        }
    }

    private Middleware delete(final String collection, final String key, @NotNull final CRUD crud) {
        return new Middleware() {
            @Override
            public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
                if (crud.deleteHandler == null) {
                    next.handle(405);
                    return;
                }

                // get the real id from the params multimap
                final String id = request.params().get(collection);

                final JsonObject filter = new JsonObject()
                        .putObject("query", new JsonObject().putString(key, id));

                final JsonObject userFilter = request.get("filter");

                if (userFilter != null) {
                    filter.mergeIn(userFilter);
                }

                crud.deleteHandler.handle(filter, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject reply) {
                        if ("error".equals(reply.getString("status"))) {
                            String message = reply.getString("message");
                            if (message != null) {
                                next.handle(message);
                            } else {
                                next.handle(500);
                            }
                            return;
                        }

                        final Integer result = reply.getInteger("value");
                        if (result == null || result == 0) {
                            next.handle(404);
                        } else {
                            request.response().setStatusCode(204);
                            request.response().end();
                        }
                    }
                });
            }
        };
    }

    private Middleware create(@NotNull final String collection, final String key, @NotNull final CRUD crud) {
        return new Middleware() {
            @Override
            public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
                if (crud.createHandler == null) {
                    next.handle(405);
                    return;
                }

                final JsonObject item = request.body();

                if (item == null) {
                    next.handle("Body must be JSON");
                    return;
                }

                final JsonObject filter = new JsonObject()
                        .putObject("value", item);

                final JsonObject userFilter = request.get("filter");

                if (userFilter != null) {
                    filter.mergeIn(userFilter);
                }

                crud.createHandler.handle(filter, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject reply) {
                        if ("error".equals(reply.getString("status"))) {
                            String message = reply.getString("message");
                            if (message != null) {
                                next.handle(message);
                            } else {
                                next.handle(500);
                            }
                            return;
                        }

                        request.response().putHeader("location", request.normalizedPath() + "/" + reply.getField("value"));
                        request.response().setStatusCode(201);
                        request.response().end();
                    }
                });
            }
        };
    }

    private Middleware update(@NotNull final String collection, final String key, @NotNull final CRUD crud) {
        return new Middleware() {
            @Override
            public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
                if (crud.updateHandler == null) {
                    next.handle(405);
                    return;
                }

                final JsonObject item = request.body();

                if (item == null) {
                    next.handle("Body must be JSON");
                    return;
                }

                // get the real id from the params multimap
                String id = request.params().get(collection);

                final JsonObject filter = new JsonObject()
                        .putObject("value", item)
                        .putObject("query", new JsonObject().putString(key, id));

                final JsonObject userFilter = request.get("filter");

                if (userFilter != null) {
                    filter.mergeIn(userFilter);
                }

                crud.updateHandler.handle(filter, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject reply) {
                        if ("error".equals(reply.getString("status"))) {
                            String message = reply.getString("message");
                            if (message != null) {
                                next.handle(message);
                            } else {
                                next.handle(500);
                            }
                            return;
                        }

                        final Integer result = reply.getInteger("value");
                        if (result == null || result == 0) {
                            next.handle(404);
                        } else {
                            request.response().setStatusCode(204);
                            request.response().end();
                        }
                    }
                });
            }
        };
    }

    private Middleware read(@NotNull final String collection, final String key, @NotNull final CRUD crud) {
        return new Middleware() {
            @Override
            public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
                if (crud.readHandler == null) {
                    next.handle(405);
                    return;
                }

                // content negotiation
                if (request.accepts("application/json") == null) {
                    // Not Acceptable (we only talk json)
                    next.handle(406);
                    return;
                }

                // get the real id from the params multimap
                final String id = request.params().get(collection);

                final JsonObject filter = new JsonObject()
                        .putObject("query", new JsonObject().putString(key, id));

                final JsonObject userFilter = request.get("filter");

                if (userFilter != null) {
                    filter.mergeIn(userFilter);
                }

                crud.readHandler.handle(filter, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject reply) {
                        if ("error".equals(reply.getString("status"))) {
                            String message = reply.getString("message");
                            if (message != null) {
                                next.handle(message);
                            } else {
                                next.handle(500);
                            }
                            return;
                        }

                        final JsonArray result = reply.getArray("value");
                        if (result == null) {
                            next.handle(404);
                        } else {
                            final JsonObject item = result.get(0);
                            if (item == null) {
                                next.handle(404);
                            } else {
                                request.response().end(item);
                            }
                        }
                    }
                });
            }
        };
    }

    private Middleware append(@NotNull final String collection, final String key, @NotNull final CRUD crud) {
        return new Middleware() {
            @Override
            public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
                if (crud.readHandler == null || crud.updateHandler == null) {
                    next.handle(405);
                    return;
                }

                // get the real id from the params multimap
                final String id = request.params().get(collection);

                final JsonObject filter = new JsonObject()
                        .putObject("query", new JsonObject().putString(key, id));

                final JsonObject userFilter = request.get("filter");

                if (userFilter != null) {
                    filter.mergeIn(userFilter);
                }

                crud.readHandler.handle(filter, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject reply) {
                        if ("error".equals(reply.getString("status"))) {
                            String message = reply.getString("message");
                            if (message != null) {
                                next.handle(message);
                            } else {
                                next.handle(500);
                            }
                            return;
                        }

                        final String ifMatch = request.getHeader("If-Match");
                        final String ifNoneMatch = request.getHeader("If-None-Match");

                        // merge existing json with incoming one
                        final boolean overwrite =
                                // pure PUT, must exist and will be updated
                                (ifMatch == null && ifNoneMatch == null) ||
                                        // must exist and will be updated
                                        ("*".equals(ifMatch));

                        final JsonObject result = reply.getObject("value");

                        if (result == null) {
                            // does not exist but was marked as overwrite
                            if (overwrite) {
                                // does not exist, returns 412
                                next.handle(412);
                            } else {
                                // does not exist, returns 404
                                next.handle(404);
                            }
                        } else {
                            // does exist but was marked as not overwrite
                            if (!overwrite) {
                                // does exist, returns 412
                                next.handle(412);
                            } else {
                                result.mergeIn((JsonObject) request.body());

                                final JsonObject filter = new JsonObject()
                                        .putObject("value", result)
                                        .putObject("query", new JsonObject().putString(key, id));

                                final JsonObject userFilter = request.get("filter");

                                if (userFilter != null) {
                                    filter.mergeIn(userFilter);
                                }

                                // update back to the db
                                crud.updateHandler.handle(filter, new Handler<JsonObject>() {
                                    @Override
                                    public void handle(JsonObject reply) {
                                        if ("error".equals(reply.getString("status"))) {
                                            String message = reply.getString("message");
                                            if (message != null) {
                                                next.handle(message);
                                            } else {
                                                next.handle(500);
                                            }
                                            return;
                                        }

                                        final Integer result = reply.getInteger("value");
                                        if (result == null || result == 0) {
                                            next.handle(404);
                                        } else {
                                            request.response().setStatusCode(204);
                                            request.response().end();
                                        }
                                    }
                                });
                            }
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

    private Middleware query(@NotNull final String collection, final String key, @NotNull final CRUD crud) {

        return new Middleware() {
            @Override
            public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
                if (crud.readHandler == null) {
                    next.handle(405);
                    return;
                }

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
                    Matcher m = RANGE.matcher(range);
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
                        Matcher sort = SORT.matcher(entry.getKey());

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

                final JsonObject filter = new JsonObject()
                        .putObject("query", dbquery)
                        .putObject("sort", dbsort)
                        .putNumber("start", start)
                        .putNumber("end", end);

                final JsonObject userFilter = request.get("filter");

                if (userFilter != null) {
                    filter.mergeIn(userFilter);
                }

                crud.readHandler.handle(filter, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject reply) {
                        if ("error".equals(reply.getString("status"))) {
                            String message = reply.getString("message");
                            if (message != null) {
                                next.handle(message);
                            } else {
                                next.handle(500);
                            }
                            return;
                        }

                        final JsonArray result = reply.getArray("value");

                        if (result == null) {
                            next.handle(404);
                        } else {
                            if (range != null) {
                                // need to send the content-range with totals
                                Long count = reply.getLong("count");

                                if (count != null) {
                                    Integer realEnd = end;

                                    if (start != null && end != null) {
                                        realEnd = start + result.size();
                                    }

                                    request.response().putHeader("content-range", "items " + start + "-" + realEnd + "/" + count);

                                }
                            }

                            request.response().end(result);
                        }
                    }
                });
            }
        };
    }
}
