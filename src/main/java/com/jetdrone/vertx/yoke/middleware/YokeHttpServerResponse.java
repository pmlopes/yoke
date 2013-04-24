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
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerResponse;

import java.util.Map;

public class YokeHttpServerResponse implements HttpServerResponse {
    // the original request
    private final HttpServerResponse response;
    // the context
    private final Map<String, Object> renderContext;
    // engine map
    private final Map<String, Engine> renderEngines;

    // extra handlers
    private Handler<Void> headersHandler;
    private boolean headersHandlerTriggered;
    private Handler<Void> endHandler;

    public YokeHttpServerResponse(HttpServerResponse response, Map<String, Object> renderContext, Map<String, Engine> renderEngines) {
        this.response = response;
        this.renderContext = renderContext;
        this.renderEngines = renderEngines;
    }

    public void render(final String template, final Handler<Object> next) {
        int sep = template.lastIndexOf('.');
        if (sep != -1) {
            String extension = template.substring(sep + 1);

            Engine renderEngine = renderEngines.get(extension);

            if (renderEngine == null) {
                next.handle("No engine registered for extension: " + extension);
            } else {
                renderEngine.render(template, renderContext, new AsyncResultHandler<Buffer>() {
                    @Override
                    public void handle(AsyncResult<Buffer> asyncResult) {
                        if (asyncResult.failed()) {
                            next.handle(asyncResult.cause());
                        } else {
                            end(asyncResult.result());
                        }
                    }
                });
            }
        } else {
            next.handle("Cannot determine the extension of the template");
        }
    }

    void headersHandler(Handler<Void> handler) {
        this.headersHandler = handler;
        headersHandlerTriggered = false;
    }

    void endHandler(Handler<Void> handler) {
        this.endHandler = handler;
    }

    private void triggerHeadersHandler() {
        if (headersHandler != null && !headersHandlerTriggered) {
            headersHandlerTriggered = true;
            headersHandler.handle(null);
        }
    }

    @Override
    public int getStatusCode() {
        return response.getStatusCode();
    }

    @Override
    public HttpServerResponse setStatusCode(int statusCode) {
        return response.setStatusCode(statusCode);
    }

    @Override
    public String getStatusMessage() {
        return response.getStatusMessage();
    }

    @Override
    public HttpServerResponse setStatusMessage(String statusMessage) {
        return response.setStatusMessage(statusMessage);
    }

    @Override
    public HttpServerResponse setChunked(boolean chunked) {
        return response.setChunked(chunked);
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
        return response.putHeader(name, value);
    }

    @Override
    public Map<String, Object> trailers() {
        return response.trailers();
    }

    @Override
    public HttpServerResponse putTrailer(String name, Object value) {
        return response.putTrailer(name, value);
    }

    @Override
    public HttpServerResponse closeHandler(Handler<Void> handler) {
        return response.closeHandler(handler);
    }

    @Override
    public HttpServerResponse write(Buffer chunk) {
        triggerHeadersHandler();
        return response.write(chunk);
    }

    @Override
    public HttpServerResponse setWriteQueueMaxSize(int maxSize) {
        return response.setWriteQueueMaxSize(maxSize);
    }

    @Override
    public boolean writeQueueFull() {
        return response.writeQueueFull();
    }

    @Override
    public HttpServerResponse drainHandler(Handler<Void> handler) {
        return response.drainHandler(handler);
    }

    @Override
    public HttpServerResponse write(String chunk, String enc) {
        triggerHeadersHandler();
        return response.write(chunk, enc);
    }

    @Override
    public HttpServerResponse write(String chunk) {
        triggerHeadersHandler();
        return response.write(chunk);
    }

    @Override
    public void end(String chunk) {
        triggerHeadersHandler();
        response.end(chunk);
        if (endHandler != null) {
            endHandler.handle(null);
        }
    }

    @Override
    public void end(String chunk, String enc) {
        triggerHeadersHandler();
        response.end(chunk, enc);
        if (endHandler != null) {
            endHandler.handle(null);
        }
    }

    @Override
    public void end(Buffer chunk) {
        triggerHeadersHandler();
        response.end(chunk);
        if (endHandler != null) {
            endHandler.handle(null);
        }
    }

    @Override
    public void end() {
        triggerHeadersHandler();
        response.end();
        if (endHandler != null) {
            endHandler.handle(null);
        }
    }

    @Override
    public HttpServerResponse sendFile(String filename) {
        triggerHeadersHandler();
        return response.sendFile(filename);
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public HttpServerResponse exceptionHandler(Handler<Throwable> handler) {
        return response.exceptionHandler(handler);
    }
}
