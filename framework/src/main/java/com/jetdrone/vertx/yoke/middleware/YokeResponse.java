// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.middleware
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.MimeType;
import com.jetdrone.vertx.yoke.middleware.filters.WriterFilter;
import com.jetdrone.vertx.yoke.util.YokeException;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.ServerCookieEncoder;
import org.vertx.java.core.streams.Pump;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.streams.ReadStream;

import java.util.*;

// # YokeResponse
public class YokeResponse implements HttpServerResponse {
    // the original request
    private final HttpServerResponse response;
    // the context
    private final Map<String, Object> context;
    // engine map
    private final Map<String, Engine> engines;
    // response cookies
    private Set<Cookie> cookies;

    // extra handlers
    private List<Handler<Void>> headersHandler;
    private boolean headersHandlerTriggered;
    private List<Handler<Void>> endHandler;

    // writer filter
    private WriterFilter filter;
    private boolean hasBody;

    public YokeResponse(HttpServerResponse response, Map<String, Object> context, Map<String, Engine> engines) {
        this.response = response;
        this.context = context;
        this.engines = engines;
    }

    // protected extension

    void setFilter(WriterFilter filter) {
        this.filter = filter;
    }

    // extension to default interface

    public YokeResponse setContentType(String contentType) {
        setContentType(contentType, MimeType.getCharset(contentType));
        return this;
    }

    public YokeResponse setContentType(String contentType, String contentEncoding) {
        if (contentEncoding == null) {
            putHeader("content-type", contentType);
        } else {
            putHeader("content-type", contentType + ";charset=" + contentEncoding);
        }
        return this;
    }

    public void render(final String template, final Handler<Object> next) {
        int sep = template.lastIndexOf('.');
        if (sep != -1) {
            String extension = template.substring(sep + 1);

            final Engine renderEngine = engines.get(extension);

            if (renderEngine == null) {
                next.handle("No engine registered for extension: " + extension);
            } else {
                renderEngine.render(template, context, new AsyncResultHandler<Buffer>() {
                    @Override
                    public void handle(AsyncResult<Buffer> asyncResult) {
                        if (asyncResult.failed()) {
                            next.handle(asyncResult.cause());
                        } else {
                            setContentType(renderEngine.contentType(), renderEngine.contentEncoding());
                            end(asyncResult.result());
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
                    int errorCode;
                    // if the error was set on the response use it
                    if (getStatusCode() >= 400) {
                        errorCode = getStatusCode();
                    } else {
                        // if it was set as the error object use it
                        if (error instanceof Number) {
                            errorCode = ((Number) error).intValue();
                        } else if (error instanceof YokeException) {
                            errorCode = ((YokeException) error).getErrorCode().intValue();
                        } else {
                            // default error code
                            errorCode = 500;
                        }
                    }

                    setStatusCode(errorCode);
                    setStatusMessage(HttpResponseStatus.valueOf(errorCode).reasonPhrase());
                    end(HttpResponseStatus.valueOf(errorCode).reasonPhrase());
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
        if (headers().contains(name)) {
            return getHeader(name);
        } else {
            return defaultValue;
        }
    }

    public void redirect(String url) {
        redirect(302, url);
    }

    public void redirect(int status, String url) {
        setStatusCode(status);
        setStatusMessage(HttpResponseStatus.valueOf(status).reasonPhrase());
        putHeader("location", url);
        end();
    }

    public void end(JsonElement json) {
        if (json.isArray()) {
            JsonArray jsonArray = json.asArray();
            setContentType("application/json", "UTF-8");
            end(jsonArray.encode());
        } else if (json.isObject()) {
            JsonObject jsonObject = json.asObject();
            setContentType("application/json", "UTF-8");
            end(jsonObject.encode());
        }
    }

    public void jsonp(JsonElement json) {
        jsonp("callback", json);
    }

    public void jsonp(String callback, JsonElement json) {

        if (callback == null) {
            // treat as normal json response
            end(json);
            return;
        }

        String body = null;

        if (json != null) {
            if (json.isArray()) {
                JsonArray jsonArray = json.asArray();
                body = jsonArray.encode();
            } else if (json.isObject()) {
                JsonObject jsonObject = json.asObject();
                body = jsonObject.encode();
            }
        }

        jsonp(callback, body);
    }

    public void jsonp(String body) {
        jsonp("callback", body);
    }

    public void jsonp(String callback, String body) {

        if (callback == null) {
            // treat as normal json response
            setContentType("application/json", "UTF-8");
            end(body);
            return;
        }

        if (body == null) {
            body = "null";
        }

        // replace special chars
        body = body.replaceAll("\\u2028", "\\\\u2028").replaceAll("\\u2029", "\\\\u2029");

        // content-type
        setContentType("text/javascript", "UTF-8");
        String cb = callback.replaceAll("[^\\[\\]\\w$.]", "");
        end(cb + " && " + cb + "(" + body + ");");
    }

    public void end(ReadStream<?> stream) {
        // TODO: filter stream?
        hasBody = true;
        filter = null;
        triggerHeadersHandlers();
        Pump.createPump(stream, response).start();
        stream.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                response.end();
                triggerEndHandlers();
            }
        });
    }

    public YokeResponse addCookie(Cookie cookie) {
        if (cookies == null) {
            cookies = new TreeSet<>();
        }
        cookies.add(cookie);
        return this;
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
        if (!headersHandlerTriggered) {
            headersHandlerTriggered = true;
            // if there are handlers call them
            if (headersHandler != null) {
                for (Handler<Void> handler : headersHandler) {
                    handler.handle(null);
                }
            }
            // convert the cookies set to the right header
            if (cookies != null) {
                response.putHeader("set-cookie", ServerCookieEncoder.encode(cookies));
            }

            // if there is a filter then set the right header
            if (filter != null) {
                // verify if the filter can filter this content
                if (filter.canFilter(response.headers().get("content-type"))) {
                    response.putHeader("content-encoding", filter.encoding());
                } else {
                    // disable the filter
                    filter = null;
                }
            }
            // if there is no content delete content-type, content-encoding
            if (!hasBody) {
                response.headers().remove("content-encoding");
                response.headers().remove("content-type");
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
    public MultiMap headers() {
        return response.headers();
    }

    @Override
    public HttpServerResponse putHeader(String name, String value) {
        response.putHeader(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(CharSequence name, CharSequence value) {
        response.putHeader(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(String name, Iterable<String> values) {
        response.putHeader(name, values);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(CharSequence name, Iterable<CharSequence> values) {
        response.putHeader(name, values);
        return this;
    }

    @Override
    public MultiMap trailers() {
        return response.trailers();
    }

    @Override
    public HttpServerResponse putTrailer(String name, String value) {
        response.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putTrailer(CharSequence name, CharSequence value) {
        response.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putTrailer(String name, Iterable<String> values) {
        response.putTrailer(name, values);
        return this;
    }

    @Override
    public HttpServerResponse putTrailer(CharSequence name, Iterable<CharSequence> value) {
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
        hasBody = true;
        triggerHeadersHandlers();
        if (filter == null) {
            response.write(chunk);
        } else {
            filter.write(chunk);
        }
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
        hasBody = true;
        triggerHeadersHandlers();
        if (filter == null) {
            response.write(chunk, enc);
        } else {
            filter.write(chunk, enc);
        }
        return this;
    }

    @Override
    public HttpServerResponse write(String chunk) {
        hasBody = true;
        triggerHeadersHandlers();
        if (filter == null) {
            response.write(chunk);
        } else {
            filter.write(chunk);
        }
        return this;
    }

    @Override
    public void end(String chunk) {
        hasBody = true;
        triggerHeadersHandlers();
        if (filter == null) {
            response.end(chunk);
        } else {
            response.end(filter.end(chunk));
        }
        triggerEndHandlers();
    }

    @Override
    public void end(String chunk, String enc) {
        hasBody = true;
        triggerHeadersHandlers();
        if (filter == null) {
            response.end(chunk, enc);
        } else {
            response.end(filter.end(chunk, enc));
        }
        triggerEndHandlers();
    }

    @Override
    public void end(Buffer chunk) {
        hasBody = true;
        triggerHeadersHandlers();
        response.end(filter == null ? chunk : filter.end(chunk));
        triggerEndHandlers();
    }

    @Override
    public void end() {
        triggerHeadersHandlers();
        response.end();
        triggerEndHandlers();
    }

    @Override
    public YokeResponse sendFile(String filename) {
        // TODO: filter file?
        hasBody = true;
        filter = null;
        triggerHeadersHandlers();
        response.sendFile(filename);
        return this;
    }

    @Override
    public YokeResponse sendFile(String filename, String notFoundFile) {
        // TODO: filter file?
        hasBody = true;
        filter = null;
        triggerHeadersHandlers();
        response.sendFile(filename, notFoundFile);
        return this;
    }

    @Override
    public YokeResponse sendFile(String filename, Handler<AsyncResult<Void>> resultHandler) {
        // TODO: filter file?
        hasBody = true;
        filter = null;
        triggerHeadersHandlers();
        response.sendFile(filename, resultHandler);
        return this;
    }

    @Override
    public YokeResponse sendFile(String filename, String notFoundFile, Handler<AsyncResult<Void>> resultHandler) {
        // TODO: filter file?
        hasBody = true;
        filter = null;
        triggerHeadersHandlers();
        response.sendFile(filename, notFoundFile, resultHandler);
        return this;
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public YokeResponse exceptionHandler(Handler<Throwable> handler) {
        response.exceptionHandler(handler);
        return this;
    }
}
