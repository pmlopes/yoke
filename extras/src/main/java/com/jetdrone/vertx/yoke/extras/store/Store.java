package com.jetdrone.vertx.yoke.extras.store;

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public interface Store {
    /**
     * Creates a new Object in the database and asynchronously returns the id for this new object.
     */
    void create(String entity, JsonObject object, AsyncResultHandler<String> response);

    /**
     * Reads a object from the database given the id.
     */
    void read(String entity, String id, AsyncResultHandler<JsonObject> response);

    /**
     * Updates a object. Returns the total updated elements
     */
    void update(String entity, String id, JsonObject object, AsyncResultHandler<Number> response);

    /**
     * Deletes a object given an id. Returns the total number of removed elements.
     */
    void delete(String entity, String id, AsyncResultHandler<Number> response);

    void query(String entity, JsonObject query, Number start, Number end, JsonObject sort, AsyncResultHandler<JsonArray> response);
    void count(String entity, JsonObject query, AsyncResultHandler<Number> response);
}
