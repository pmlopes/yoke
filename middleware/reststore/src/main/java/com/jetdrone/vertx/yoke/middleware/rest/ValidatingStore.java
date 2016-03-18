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

import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ValidatingStore extends AbstractValidatingStore {

    public ValidatingStore(Store store) {
        super(store);
    }

    private static final YokeAsyncResult<String> OK_STRING = new YokeAsyncResult<>(null, null);
    private static final YokeAsyncResult<JsonObject> OK_JSONOBJECT = new YokeAsyncResult<>(null, null);
    private static final YokeAsyncResult<JsonArray> OK_JSONARRAY = new YokeAsyncResult<>(null, null);
    private static final YokeAsyncResult<Number> OK_NUMBER = new YokeAsyncResult<>(null, null);

    public void beforeCreate(String entity, JsonObject object, AsyncResultHandler<String> response) {
        response.handle(OK_STRING);
    }

    public void afterCreate(String entity, JsonObject object, AsyncResultHandler<String> response) {
        response.handle(OK_STRING);
    }

    public void beforeRead(String entity, String id, AsyncResultHandler<JsonObject> response) {
        response.handle(OK_JSONOBJECT);
    }

    public void afterRead(String entity, String id, AsyncResultHandler<JsonObject> response) {
        response.handle(OK_JSONOBJECT);
    }

    public void beforeUpdate(String entity, String id, JsonObject object, AsyncResultHandler<Number> response) {
        response.handle(OK_NUMBER);
    }

    public void afterUpdate(String entity, String id, JsonObject object, AsyncResultHandler<Number> response) {
        response.handle(OK_NUMBER);
    }

    public void beforeDelete(String entity, String id, AsyncResultHandler<Number> response) {
        response.handle(OK_NUMBER);
    }

    public void afterDelete(String entity, String id, AsyncResultHandler<Number> response) {
        response.handle(OK_NUMBER);
    }

    public void beforeQuery(String entity, JsonObject query, Number start, Number end, JsonObject sort, AsyncResultHandler<JsonArray> response) {
        response.handle(OK_JSONARRAY);
    }

    public void afterQuery(String entity, JsonObject query, Number start, Number end, JsonObject sort, AsyncResultHandler<JsonArray> response) {
        response.handle(OK_JSONARRAY);
    }

    public void beforeCount(String entity, JsonObject query, AsyncResultHandler<Number> response) {
        response.handle(OK_NUMBER);
    }

    public void afterCount(String entity, JsonObject query, AsyncResultHandler<Number> response) {
        response.handle(OK_NUMBER);
    }
}
