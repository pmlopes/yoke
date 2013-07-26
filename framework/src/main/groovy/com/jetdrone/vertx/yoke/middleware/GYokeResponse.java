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
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Engine;
import groovy.lang.Closure;
import org.vertx.groovy.core.impl.DefaultMultiMap;
import org.vertx.java.core.Handler;
import org.vertx.groovy.core.MultiMap;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.groovy.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;
import java.util.Map;

public class GYokeResponse extends YokeResponse /*implements org.vertx.groovy.core.http.HttpServerResponse*/ {

    private MultiMap headers;
    private MultiMap trailers;

    public GYokeResponse(HttpServerResponse response, Map<String, Object> context, Map<String, Engine> engines) {
        super(response, context, engines);
    }

    public void closeHandler(final Closure closure) {
        this.closeHandler(new Handler<Void>() {
            @Override
            public void handle(Void v) {
                closure.call();
            }
        });
    }

    public GYokeResponse write(Buffer buffer) {
        write(buffer.toJavaBuffer());
        return this;
    }

    public GYokeResponse drainHandler(final Closure closure) {
        this.drainHandler(new Handler<Void>() {
            @Override
            public void handle(Void v) {
                closure.call();
            }
        });
        return this;
    }

    public void end(Buffer buffer) {
        end(buffer.toJavaBuffer());
    }

    public GYokeResponse leftShift(Buffer buffer) {
        return write(buffer);
    }

    public GYokeResponse leftShift(String s) {
        write(s);
        return this;
    }

    public GYokeResponse exceptionHandler(final Closure closure) {
        this.exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable exception) {
                closure.call();
            }
        });

        return this;
    }

    public MultiMap getHeaders() {
        if (headers == null) {
            headers = new DefaultMultiMap(headers());
        }
        return headers;
    }

    public MultiMap getTrailers() {
        if (trailers == null) {
            trailers = new DefaultMultiMap(trailers());
        }
        return trailers;
    }

    public boolean isWriteQueueFull() {
        return writeQueueFull();
    }

    public void end(Map<String, Object> json) {
        setContentType("application/json", "UTF-8");
        end(new JsonObject(json).encode());
    }

    public void end(List<Object> json) {
        setContentType("application/json", "UTF-8");
        end(new JsonArray(json).encode());
    }

    public void jsonp(Map<String, Object> json) {
        jsonp("callback", json);
    }

    public void jsonp(List<Object> json) {
        jsonp("callback", json);
    }

    public void jsonp(String callback, Map<String, Object> json) {

        if (callback == null) {
            // treat as normal json response
            end(json);
            return;
        }

        String body = null;

        if (json != null) {
            JsonObject jsonObject = new JsonObject(json);
            body = jsonObject.encode();
        }

        jsonp(callback, body);
    }

    public void jsonp(String callback, List<Object> json) {

        if (callback == null) {
            // treat as normal json response
            end(json);
            return;
        }

        String body = null;

        if (json != null) {
            JsonArray jsonArray = new JsonArray(json);
            body = jsonArray.encode();
        }

        jsonp(callback, body);
    }
}
