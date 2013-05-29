package com.jetdrone.vertx.yoke.extras.stores;

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public interface Store {
    /**
     * Creates a new Object in the database and asynchronously returns the id for this new object.
     */
    void create(JsonObject object, AsyncResultHandler<String> response);

    /**
     * Reads a object from the database given the id.
     */
    void read(String id, AsyncResultHandler<JsonObject> response);

    /**
     * Updates a object
     */
    void update(String id, JsonObject object, AsyncResultHandler<Void> response);

    /**
     * Deletes a object given an id.
     */
    void delete(String id, AsyncResultHandler<Void> response);

    void query(String query, AsyncResultHandler<JsonArray> response);
    void count(String query, AsyncResultHandler<Long> response);
}
