package com.jetdrone.vertx.yoke.extras.store;

import com.jetdrone.vertx.yoke.util.YokeAsyncResult;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

// TODO: need a validation middleware, cannot just accept whatever comes from the network to the database
public class ValidatingStore implements Store {

    private final Store baseStore;

    public ValidatingStore(Store store) {
        this.baseStore = store;
    }

    private static final YokeAsyncResult OK = new YokeAsyncResult(null, null);

    public void beforeCreate(String entity, JsonObject object, AsyncResultHandler<String> response) {
        response.handle(OK);
    }

    @Override
    public void create(final String entity, final JsonObject object, final AsyncResultHandler<String> response) {
        beforeCreate(entity, object, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> event) {
                if (event.failed()) {
                    response.handle(event);
                    return;
                }

                baseStore.create(entity, object, response);
            }
        });
    }

    public void beforeRead(String entity, String id, AsyncResultHandler<JsonObject> response) {
        response.handle(OK);
    }

    @Override
    public void read(final String entity, final String id, final AsyncResultHandler<JsonObject> response) {
        beforeRead(entity, id, new AsyncResultHandler<JsonObject>() {
            @Override
            public void handle(AsyncResult<JsonObject> event) {
                if (event.failed()) {
                    response.handle(event);
                    return;
                }

                baseStore.read(entity, id, response);
            }
        });
    }

    public void beforeUpdate(String entity, String id, JsonObject object, AsyncResultHandler<Number> response) {
        response.handle(OK);
    }

    @Override
    public void update(final String entity, final String id, final JsonObject object, final AsyncResultHandler<Number> response) {
        beforeUpdate(entity, id, object, new AsyncResultHandler<Number>() {
            @Override
            public void handle(AsyncResult<Number> event) {
                if (event.failed()) {
                    response.handle(event);
                    return;
                }

                baseStore.update(entity, id, object, response);
            }
        });
    }

    public void beforeDelete(String entity, String id, AsyncResultHandler<Number> response) {
        response.handle(OK);
    }

    @Override
    public void delete(final String entity, final String id, final AsyncResultHandler<Number> response) {
        beforeDelete(entity, id, new AsyncResultHandler<Number>() {
            @Override
            public void handle(AsyncResult<Number> event) {
                if (event.failed()) {
                    response.handle(event);
                    return;
                }

                baseStore.delete(entity, id, response);
            }
        });
    }

    public void beforeQuery(String entity, JsonObject query, Number start, Number end, JsonObject sort, AsyncResultHandler<JsonArray> response) {
        response.handle(OK);
    }

    @Override
    public void query(final String entity, final JsonObject query, final Number start, final Number end, final JsonObject sort, final AsyncResultHandler<JsonArray> response) {
        beforeQuery(entity, query, start, end, sort, new AsyncResultHandler<JsonArray>() {
            @Override
            public void handle(AsyncResult<JsonArray> event) {
                if (event.failed()) {
                    response.handle(event);
                    return;
                }

                baseStore.query(entity, query, start, end, sort, response);
            }
        });
    }

    public void beforeCount(String entity, JsonObject query, AsyncResultHandler<Number> response) {
        response.handle(OK);
    }

    @Override
    public void count(final String entity, final JsonObject query, final AsyncResultHandler<Number> response) {
        beforeCount(entity, query, new AsyncResultHandler<Number>() {
            @Override
            public void handle(AsyncResult<Number> event) {
                if (event.failed()) {
                    response.handle(event);
                    return;
                }

                baseStore.count(entity, query, response);
            }
        });
    }
}
