/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.test;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveMultiMap;
import io.vertx.core.http.HttpServerResponse;

/** # Response */
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
        vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                handler.handle(Response.this);
            }
        });
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
    public HttpServerResponse putHeader(CharSequence name, CharSequence value) {
        headers().set(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(String name, Iterable<String> values) {
        headers().set(name, values);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(CharSequence name, Iterable<CharSequence> values) {
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
    public HttpServerResponse putTrailer(CharSequence name, CharSequence value) {
        trailers().set(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putTrailer(String name, Iterable<String> values) {
        trailers().set(name, values);
        return this;
    }

    @Override
    public HttpServerResponse putTrailer(CharSequence name, Iterable<CharSequence> value) {
        trailers().set(name, value);
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
        done();
        return this;
    }

    @Override
    public HttpServerResponse sendFile(String filename, String notFoundFile) {
        throw new UnsupportedOperationException("This mock does not support sendFile with 2 args");
    }

    @Override
    public HttpServerResponse sendFile(String filename, Handler<AsyncResult<Void>> resultHandler) {
        body.appendBuffer(vertx.fileSystem().readFileSync(filename));
        resultHandler.handle(new AsyncResult<Void>() {
            @Override
            public Void result() {
                return null;
            }

            @Override
            public Throwable cause() {
                return null;
            }

            @Override
            public boolean succeeded() {
                return true;
            }

            @Override
            public boolean failed() {
                return false;
            }
        });
        return this;
    }

    @Override
    public HttpServerResponse sendFile(String filename, String notFoundFile, Handler<AsyncResult<Void>> resultHandler) {
        throw new UnsupportedOperationException("This mock does not support sendFile with 2 args + Handler");
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
