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
package com.jetdrone.vertx.yoke.test;

import org.vertx.java.core.impl.CaseInsensitiveMultiMap;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerResponse;

public class Response implements HttpServerResponse {

    public int statusCode = 200;
    public String statusMessage = "OK";
    public boolean chunked = false;
    public MultiMap headers = new CaseInsensitiveMultiMap();
    public MultiMap trailers = new CaseInsensitiveMultiMap();

    public Buffer body = new Buffer(0);

    private Handler<Void> closeHandler = null;
    private final Handler<Response> handler;
    private final Vertx vertx;

    public Response(Vertx vertx, Handler<Response> handler) {
        this.vertx = vertx;
        this.handler = handler;
    }

    private void done() {
        if (closeHandler != null) {
            closeHandler.handle(null);
        }
        // complete
        handler.handle(this);
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public HttpServerResponse setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public HttpServerResponse setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    @Override
    public HttpServerResponse setChunked(boolean chunked) {
        this.chunked = chunked;
        return this;
    }

    @Override
    public boolean isChunked() {
        return chunked;
    }

    @Override
    public MultiMap headers() {
        return headers;
    }

    @Override
    public HttpServerResponse putHeader(String name, String value) {
        headers().set(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(String name, Iterable<String> values) {
        headers().set(name, values);
        return this;
    }

    @Override
    public MultiMap trailers() {
        return trailers;
    }

    @Override
    public HttpServerResponse putTrailer(String name, String value) {
        trailers().set(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putTrailer(String name, Iterable<String> values) {
        trailers().set(name, values);
        return this;
    }

    @Override
    public HttpServerResponse closeHandler(Handler<Void> handler) {
        closeHandler = handler;
        return this;
    }

    @Override
    public HttpServerResponse write(Buffer chunk) {
        body.appendBuffer(chunk);
        return this;
    }

    @Override
    public HttpServerResponse write(String chunk, String enc) {
        body.appendString(chunk, enc);
        return this;
    }

    @Override
    public HttpServerResponse write(String chunk) {
        body.appendString(chunk);
        return this;
    }

    @Override
    public void end(String chunk) {
        body.appendString(chunk);
        done();
    }

    @Override
    public void end(String chunk, String enc) {
        body.appendString(chunk, enc);
        done();
    }

    @Override
    public void end(Buffer chunk) {
        body.appendBuffer(chunk);
        done();
    }

    @Override
    public void end() {
        done();
    }

    @Override
    public HttpServerResponse sendFile(String filename) {
        body.appendBuffer(vertx.fileSystem().readFileSync(filename));
        return this;
    }

    @Override
    public HttpServerResponse sendFile(String filename, String notFoundFile) {
        throw new UnsupportedOperationException("This mock does not support sendFile with 2 args");
    }

    @Override
    public void close() {
        done();
    }

    @Override
    public HttpServerResponse setWriteQueueMaxSize(int maxSize) {
        // NOOP
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        return false;
    }

    @Override
    public HttpServerResponse drainHandler(Handler<Void> handler) {
        throw new UnsupportedOperationException("This mock does not support drainHandler");
    }

    @Override
    public HttpServerResponse exceptionHandler(Handler<Throwable> handler) {
        throw new UnsupportedOperationException("This mock does not support exceptionHandler");
    }
}
