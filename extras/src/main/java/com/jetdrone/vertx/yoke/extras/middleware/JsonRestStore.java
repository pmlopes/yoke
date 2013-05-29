package com.jetdrone.vertx.yoke.extras.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.extras.stores.Store;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonRestStore extends Middleware {

    // GET /
    public static final int QUERY =     1;
    // GET /:id
    public static final int READ =      2;
    // PUT /:id
    public static final int UPDATE =    4;
    // POST /:id
    public static final int APPEND =    8;
    // POST /
    public static final int CREATE =    16;
    // DELETE /:id
    public static final int DELETE =    32;

    private final Pattern idPattern;
    private final int allowedOperations;
    private final String resource;
    private final Store store;

    public JsonRestStore(Store store, String resource, int allowedOperations) {
        this.store = store;
        this.resource = resource;
        idPattern = Pattern.compile(resource + "/(.+)$");
        this.allowedOperations = allowedOperations;
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
        store.delete(id, new AsyncResultHandler<Void>() {
            @Override
            public void handle(AsyncResult<Void> event) {
                if (event.failed()) {
                    next.handle(event.cause());
                    return;
                }
                request.response().setStatusCode(204);
                request.response().end();
            }
        });
    }

    private void create(final YokeRequest request, final Handler<Object> next) {
        store.create(request.jsonBody(), new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> event) {
                if (event.failed()) {
                    next.handle(event.cause());
                    return;
                }
                request.response().putHeader("location", resource + "/" + event.result());
                request.response().setStatusCode(201);
                request.response().end();
            }
        });
    }

    private void append(YokeRequest request, String id, Handler<Object> next) {
        System.out.println("append");
        //To change body of created methods use File | Settings | File Templates.
    }

    private void update(YokeRequest request, String id, Handler<Object> next) {
        System.out.println("update");
        //To change body of created methods use File | Settings | File Templates.
    }

    private void query(YokeRequest request, Handler<Object> next) {
        System.out.println("query");
        //To change body of created methods use File | Settings | File Templates.
    }

    private void read(final YokeRequest request, String id, final Handler<Object> next) {
        store.read(id, new AsyncResultHandler<JsonObject>() {
            @Override
            public void handle(AsyncResult<JsonObject> event) {
                if (event.failed()) {
                    next.handle(event.cause());
                    return;
                }
                request.response().end(event.result());
            }
        });
    }
}
