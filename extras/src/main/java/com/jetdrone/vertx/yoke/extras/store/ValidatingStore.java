package com.jetdrone.vertx.yoke.extras.store;

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

// TODO: need a validation middleware, cannot just accept whatever comes from the network to the database
public abstract class ValidatingStore implements Store {

    private final Store baseStore;

    public ValidatingStore(Store store) {
        this.baseStore = store;
    }

    @Override
    public void create(String entity, JsonObject object, AsyncResultHandler<String> response) {
        // call before
        // call baseStore
        // call after
    }

    @Override
    public void read(String entity, String id, AsyncResultHandler<JsonObject> response) {
        // call before
        // call baseStore
        // call after
    }

    @Override
    public void update(String entity, String id, JsonObject object, AsyncResultHandler<Number> response) {
        // call before
        // call baseStore
        // call after
    }

    @Override
    public void delete(String entity, String id, AsyncResultHandler<Number> response) {
        // call before
        // call baseStore
        // call after
    }

    @Override
    public void query(String entity, JsonObject query, String start, String end, JsonObject sort, AsyncResultHandler<JsonArray> response) {
        // call before
        // call baseStore
        // call after
    }

    @Override
    public void count(String entity, JsonObject query, AsyncResultHandler<Number> response) {
        // call before
        // call baseStore
        // call after
    }
}
