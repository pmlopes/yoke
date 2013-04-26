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
import io.netty.handler.codec.http.HttpResponseStatus;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YokeHttpServerResponse implements HttpServerResponse {
    // the original request
    private final HttpServerResponse response;
    // the context
    private final Map<String, Object> context;
    // engine map
    private final Map<String, Engine> engines;

    // extra handlers
    private List<Handler<Void>> headersHandler;
    private boolean headersHandlerTriggered;
    private List<Handler<Void>> endHandler;

    public YokeHttpServerResponse(HttpServerResponse response, Map<String, Object> context, Map<String, Engine> engines) {
        this.response = response;
        this.context = context;
        this.engines = engines;
    }

    // extension to default interface

    public void render(final String template, final Handler<Object> next) {
        int sep = template.lastIndexOf('.');
        if (sep != -1) {
            String extension = template.substring(sep + 1);

            final Engine renderEngine = engines.get(extension);

            if (renderEngine == null) {
                next.handle("No engine registered for extension: " + extension);
            } else {
                renderEngine.exists(template, new Handler<Boolean>() {
                    @Override
                    public void handle(Boolean exists) {
                        if (!exists) {
                            next.handle(404);
                        } else {
                            renderEngine.render(template, context, new AsyncResultHandler<Buffer>() {
                                @Override
                                public void handle(AsyncResult<Buffer> asyncResult) {
                                    if (asyncResult.failed()) {
                                        next.handle(asyncResult.cause());
                                    } else {
                                        putHeader("content-type", renderEngine.getContentType());
                                        end(asyncResult.result());
                                    }
                                }
                            });
                        }
                    }
                });
            }
        } else {
            next.handle("Cannot determine the extension of the template");
        }
    }

    public void render(final String template) {
        render(template, new Handler<Object>() {
            @Override
            public void handle(Object error) {
                if (error != null) {
                    if (error instanceof Integer) {
                        setStatusCode((Integer) error);
                        setStatusMessage(HttpResponseStatus.valueOf((Integer) error).reasonPhrase());
                    } else {
                        setStatusCode(500);
                        setStatusMessage(HttpResponseStatus.valueOf(500).reasonPhrase());
                    }
                    end();
                }
            }
        });
    }

    /**
     * Allow getting headers in a generified way.
     *
     * @param name The key to get
     * @param <R> The type of the return
     * @return The found object
     */
    @SuppressWarnings("unchecked")
    public <R> R getHeader(String name) {
        return (R) headers().get(name);
    }

    /**
     * Allow getting headers in a generified way and return defaultValue if the key does not exist.
     *
     * @param name The key to get
     * @param defaultValue value returned when the key does not exist
     * @param <R> The type of the return
     * @return The found object
     */
    public <R> R getHeader(String name, R defaultValue) {
        if (headers().containsKey(name)) {
            return getHeader(name);
        } else {
            return defaultValue;
        }
    }

    public void redirect(String url) {
        redirect(302, url);
    }

    public void redirect(int status, String url) {
        response.setStatusCode(status);
        response.setStatusMessage(HttpResponseStatus.valueOf(status).reasonPhrase());
        response.putHeader("location", url);
        end();
    }

    public void end(JsonElement json) {
        if (json.isArray()) {
            JsonArray jsonArray = json.asArray();
            response.putHeader("content-type", "application/json");
            triggerHeadersHandlers();
            response.end(jsonArray.encode());
            triggerEndHandlers();
        } else if (json.isObject()) {
            JsonObject jsonObject = json.asObject();
            response.putHeader("content-type", "application/json");
            triggerHeadersHandlers();
            response.end(jsonObject.encode());
            triggerEndHandlers();
        }
    }

    public void headersHandler(Handler<Void> handler) {
        if (!headersHandlerTriggered) {
            if (headersHandler == null) {
                headersHandler = new ArrayList<>();
            }
            headersHandler.add(handler);
        }
    }

    public void endHandler(Handler<Void> handler) {
        if (endHandler == null) {
            endHandler = new ArrayList<>();
        }
        endHandler.add(handler);
    }

    private void triggerHeadersHandlers() {
        if (headersHandler != null && !headersHandlerTriggered) {
            headersHandlerTriggered = true;
            for (Handler<Void> handler : headersHandler) {
                handler.handle(null);
            }
        }
    }

    private void triggerEndHandlers() {
        if (endHandler != null) {
            for (Handler<Void> handler : endHandler) {
                handler.handle(null);
            }
        }
    }

    // interface implementation

    @Override
    public int getStatusCode() {
        return response.getStatusCode();
    }

    @Override
    public HttpServerResponse setStatusCode(int statusCode) {
        response.setStatusCode(statusCode);
        return this;
    }

    @Override
    public String getStatusMessage() {
        return response.getStatusMessage();
    }

    @Override
    public HttpServerResponse setStatusMessage(String statusMessage) {
        response.setStatusMessage(statusMessage);
        return this;
    }

    @Override
    public HttpServerResponse setChunked(boolean chunked) {
        response.setChunked(chunked);
        return this;
    }

    @Override
    public boolean isChunked() {
        return response.isChunked();
    }

    @Override
    public Map<String, Object> headers() {
        return response.headers();
    }

    @Override
    public HttpServerResponse putHeader(String name, Object value) {
        response.putHeader(name, value);
        return this;
    }

    @Override
    public Map<String, Object> trailers() {
        return response.trailers();
    }

    @Override
    public HttpServerResponse putTrailer(String name, Object value) {
        response.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpServerResponse closeHandler(Handler<Void> handler) {
        response.closeHandler(handler);
        return this;
    }

    @Override
    public HttpServerResponse write(Buffer chunk) {
        triggerHeadersHandlers();
        response.write(chunk);
        return this;
    }

    @Override
    public HttpServerResponse setWriteQueueMaxSize(int maxSize) {
        response.setWriteQueueMaxSize(maxSize);
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        return response.writeQueueFull();
    }

    @Override
    public HttpServerResponse drainHandler(Handler<Void> handler) {
        response.drainHandler(handler);
        return this;
    }

    @Override
    public HttpServerResponse write(String chunk, String enc) {
        triggerHeadersHandlers();
        response.write(chunk, enc);
        return this;
    }

    @Override
    public HttpServerResponse write(String chunk) {
        triggerHeadersHandlers();
        response.write(chunk);
        return this;
    }

    @Override
    public void end(String chunk) {
        triggerHeadersHandlers();
        response.end(chunk);
        triggerEndHandlers();
    }

    @Override
    public void end(String chunk, String enc) {
        triggerHeadersHandlers();
        response.end(chunk, enc);
        triggerEndHandlers();
    }

    @Override
    public void end(Buffer chunk) {
        triggerHeadersHandlers();
        response.end(chunk);
        triggerEndHandlers();
    }

    @Override
    public void end() {
        triggerHeadersHandlers();
        response.end();
        triggerEndHandlers();
    }

    @Override
    public HttpServerResponse sendFile(String filename) {
        triggerHeadersHandlers();
        response.sendFile(filename);
        return this;
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public HttpServerResponse exceptionHandler(Handler<Throwable> handler) {
        response.exceptionHandler(handler);
        return this;
    }

    // JavaBean accessors

    public Map<String, Object> getHeaders() {
        return headers();
    }

    public Map<String, Object> getTrailers() {
        return trailers();
    }

    public boolean isWriteQueueFull() {
        return writeQueueFull();
    }
}
