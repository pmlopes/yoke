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

import io.vertx.core.AsyncResultHandler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

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

    /**
     * Queries for a collection of objects given a query, limit and sorting.
     */
    void query(String entity, JsonObject query, Number start, Number end, JsonObject sort, AsyncResultHandler<JsonArray> response);

    /**
     * Counts the elements of a collection given a query.
     */
    void count(String entity, JsonObject query, AsyncResultHandler<Number> response);
}
