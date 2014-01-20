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
import com.jetdrone.vertx.yoke.core.GMultiMap;
import com.jetdrone.vertx.yoke.core.JSON;
import groovy.lang.Closure;
import org.vertx.groovy.core.impl.DefaultMultiMap;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.groovy.core.MultiMap;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.groovy.core.buffer.Buffer;

import java.util.List;
import java.util.Map;

public class GYokeResponse extends YokeResponse implements org.vertx.groovy.core.http.HttpServerResponse {

    private GMultiMap headers;
    private GMultiMap trailers;

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

    public GYokeResponse write(String chunk) {
        super.write(chunk);
        return this;
    }

    public GYokeResponse write(String chunk, String enc) {
        super.write(chunk, enc);
        return this;
    }

    @Override
    public GYokeResponse setStatusCode(int statusCode) {
        super.setStatusCode(statusCode);
        return this;
    }

    @Override
    public GYokeResponse putTrailer(String name, String value) {
        super.putTrailer(name, value);
        return this;
    }

    @Override
    public GYokeResponse sendFile(String filename) {
        super.sendFile(filename);
        return this;
    }

    @Override
    public GYokeResponse setWriteQueueMaxSize(int maxSize) {
        super.setWriteQueueMaxSize(maxSize);
        return this;
    }

    @Override
    public GYokeResponse setChunked(boolean chunked) {
        super.setChunked(chunked);
        return this;
    }

    @Override
    public GYokeResponse setStatusMessage(String statusMessage) {
        super.setStatusMessage(statusMessage);
        return this;
    }

    @Override
    public GYokeResponse putHeader(String name, Iterable<String> values) {
        super.putHeader(name, values);
        return this;
    }

    @Override
    public GYokeResponse putTrailer(String name, Iterable<String> values) {
        super.putTrailer(name, values);
        return this;
    }

    @Override
    public GYokeResponse sendFile(String filename, String notFoundFile) {
        super.sendFile(filename, notFoundFile);
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

    public GMultiMap getHeaders() {
        if (headers == null) {
            headers = new GMultiMap(headers());
        }
        return headers;
    }

    public GMultiMap getTrailers() {
        if (trailers == null) {
            trailers = new GMultiMap(trailers());
        }
        return trailers;
    }

    public boolean isWriteQueueFull() {
        return writeQueueFull();
    }

    public void end(Map<String, Object> json) {
        setContentType("application/json", "UTF-8");
        end(JSON.encode(json));
    }

    public void end(List<Object> json) {
        setContentType("application/json", "UTF-8");
        end(JSON.encode(json));
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
            body = JSON.encode(json);
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
            body = JSON.encode(json);
        }

        jsonp(callback, body);
    }

    public GYokeResponse sendFile(String filename, final Closure resultHandler) {
        sendFile(filename, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> event) {
                resultHandler.call(event);
            }
        });
        return this;
    }

    public GYokeResponse sendFile(String filename, String notFoundFile, final Closure resultHandler) {
        sendFile(filename, notFoundFile, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> event) {
                resultHandler.call(event);
            }
        });
        return this;
    }

    public void render(final String template, final Closure<Object> next) {
        render(template, new Handler<Object>() {
            @Override
            public void handle(Object event) {
                next.call(event);
            }
        });
    }

    @Override
    public GYokeResponse putHeader(String name, String value) {
        super.putHeader(name, value);
        return this;
    }
}
