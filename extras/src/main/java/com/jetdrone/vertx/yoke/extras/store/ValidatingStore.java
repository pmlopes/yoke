package com.jetdrone.vertx.yoke.extras.store;

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

// TODO: need a validation middleware, cannot just accept whatever comes from the network to the database
public class ValidatingStore implements Store {

    private final Store baseStore;

    public ValidatingStore(Store store) {
        this.baseStore = store;
    }

    @Override
    public void create(String entity, JsonObject object, AsyncResultHandler<String> response) {
        // call before
        baseStore.create(entity, object, response);
        // call after
    }

    @Override
    public void read(String entity, String id, AsyncResultHandler<JsonObject> response) {
        // call before
        baseStore.read(entity, id, response);
        // call after
    }

    @Override
    public void update(String entity, String id, JsonObject object, AsyncResultHandler<Number> response) {
        // call before
        baseStore.update(entity, id, object, response);
        // call after
    }

    @Override
    public void delete(String entity, String id, AsyncResultHandler<Number> response) {
        // call before
        baseStore.delete(entity, id, response);
        // call after
    }

    @Override
    public void query(String entity, JsonObject query, Number start, Number end, JsonObject sort, AsyncResultHandler<JsonArray> response) {
        // call before
        baseStore.query(entity, query, start, end, sort, response);
        // call after
    }

    @Override
    public void count(String entity, JsonObject query, AsyncResultHandler<Number> response) {
        // call before
        baseStore.count(entity, query, response);
        // call after
    }
}
