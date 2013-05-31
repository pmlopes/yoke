package com.jetdrone.vertx.yoke.extras.middleware;

import com.jetdrone.vertx.yoke.Middleware;
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

public class JsonRestStore extends Middleware {

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

    private final Pattern idPattern;
    private final int allowedOperations;
    private final String resource;
    private final Store store;
    private final String sortParam;
    private final Pattern sortPattern = Pattern.compile("sort\\((.+)\\)");

    public JsonRestStore(Store store, String resource, int allowedOperations) {
        this.store = store;
        this.resource = resource;
        idPattern = Pattern.compile(resource + "/(.+)$");
        this.allowedOperations = allowedOperations;
        this.sortParam = null;
    }

    public JsonRestStore(Store store, String resource) {
        this(store, resource, QUERY + READ + UPDATE + APPEND + CREATE + DELETE);
    }

    public JsonRestStore(Store store, int allowedOperations) {
        this(store, "", allowedOperations);
    }

    public JsonRestStore(Store store) {
        this(store, "", QUERY + READ + UPDATE + APPEND + CREATE + DELETE);
    }

    private boolean isAllowed(int operation) {
        return (allowedOperations & operation) == operation;
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {

        if (!request.path().startsWith(resource)) {
            next.handle(null);
            return;
        }

        Matcher idMatcher = idPattern.matcher(request.path());

        switch (request.method()) {
            case "GET":
                if (idMatcher.matches()) {
                    if (!isAllowed(READ)) {
                        next.handle(405);
                        return;
                    }
                    read(request, idMatcher.group(1), next);
                    return;
                }
                if (!isAllowed(QUERY)) {
                    next.handle(405);
                    return;
                }
                query(request, next);
                return;
            case "PUT":
                if (idMatcher.matches()) {
                    if (!isAllowed(UPDATE)) {
                        next.handle(405);
                        return;
                    }
                    update(request, idMatcher.group(1), next);
                    return;
                }
                break;
            case "POST":
                // shortcut for patch (as by Dojo Toolkit)
                if (idMatcher.matches()) {
                    if (!isAllowed(APPEND)) {
                        next.handle(405);
                        return;
                    }
                    append(request, idMatcher.group(1), next);
                    return;
                }
                if (!isAllowed(CREATE)) {
                    next.handle(405);
                    return;
                }
                create(request, next);
                return;
            case "PATCH":
                if (idMatcher.matches()) {
                    if (!isAllowed(APPEND)) {
                        next.handle(405);
                        return;
                    }
                    append(request, idMatcher.group(1), next);
                    return;
                }
                break;
            case "DELETE":
                if (idMatcher.matches()) {
                    if (!isAllowed(DELETE)) {
                        next.handle(405);
                        return;
                    }
                    delete(request, idMatcher.group(1), next);
                    return;
                }
                break;
        }

        // could not be handled here
        next.handle(null);
    }

    private void delete(final YokeRequest request, String id, final Handler<Object> next) {
        store.delete(id, new AsyncResultHandler<Number>() {
            @Override
            public void handle(AsyncResult<Number> event) {
                if (event.failed()) {
                    next.handle(event.cause());
                    return;
                }

                if (event.result() == 0) {
                    request.response().setStatusCode(404);
                    request.response().end();
                } else {
                    request.response().setStatusCode(204);
                    request.response().end();
                }
            }
        });
    }

    private void create(final YokeRequest request, final Handler<Object> next) {
        JsonObject item = request.jsonBody();

        if (item == null) {
            next.handle("Body must be JSON");
            return;
        }

        store.create(item, new AsyncResultHandler<String>() {
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

    private void append(final YokeRequest request, final String id, final Handler<Object> next) {
        store.read(id, new AsyncResultHandler<JsonObject>() {
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
                    obj.mergeIn(request.jsonBody());

                    // update back to the db
                    store.update(id, obj, new AsyncResultHandler<Number>() {
                        @Override
                        public void handle(AsyncResult<Number> event) {
                            if (event.failed()) {
                                next.handle(event.cause());
                                return;
                            }

                            if (event.result() == 0) {
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

    private void update(final YokeRequest request, final String id, final Handler<Object> next) {
        JsonObject item = request.jsonBody();

        if (item == null) {
            next.handle("Body must be JSON");
            return;
        }

        store.update(id, item, new AsyncResultHandler<Number>() {
            @Override
            public void handle(AsyncResult<Number> event) {
                if (event.failed()) {
                    next.handle(event.cause());
                    return;
                }

                if (event.result() == 0) {
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

    // range pattern
    private final Pattern rangePattern = Pattern.compile("items=(\\d+)-(\\d+)");

    private void query(final YokeRequest request, final Handler<Object> next) {
        // parse ranges
        final String range = request.getHeader("range");
        final String start, end;
        if (range != null) {
            Matcher m = rangePattern.matcher(range);
            if (m.matches()) {
                start = m.group(1);
                end = m.group(2);
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

        store.query(dbquery, start, end, dbsort, new AsyncResultHandler<JsonArray>() {
            @Override
            public void handle(final AsyncResult<JsonArray> query) {
                if (query.failed()) {
                    next.handle(query.cause());
                    return;
                }

                if (range != null) {
                    // need to send the content-range with totals
                    store.count(dbquery, new AsyncResultHandler<Number>() {
                        @Override
                        public void handle(AsyncResult<Number> count) {
                            if (count.failed()) {
                                next.handle(count.cause());
                                return;
                            }

                            request.response().putHeader("content-range", "items " + start + "-" + end + "/" + count.result());
                            request.response().end(query.result());
                        }
                    });
                    return;
                }

                request.response().end(query.result());
            }
        });
    }

    private void read(final YokeRequest request, String id, final Handler<Object> next) {

        store.read(id, new AsyncResultHandler<JsonObject>() {
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
}
