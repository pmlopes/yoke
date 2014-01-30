/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.core.JSON;
import io.netty.handler.codec.http.HttpHeaders;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.json.DecodeException;

import java.util.HashMap;

/** # BodyParser
 *
 * Parse request bodies, supports *application/json*, *application/x-www-form-urlencoded*, and *multipart/form-data*.
 *
 * Once data has been parsed the result is visible in the field `body` of the request.
 *
 * The body may be a map, a list, or a raw buffer if an unknown content type is provided.
 *
 * If the content type was *multipart/form-data* and there were uploaded files the files are ```files()``` returns
 * `Map<String, HttpServerFileUpload>`.
 *
 * ### Limitations
 *
 * Currently when parsing *multipart/form-data* if there are several files uploaded under the same name, only the last
 * is preserved.
 */
public class BodyParser extends Middleware {

    /**
     * Location on the file system to store the uploaded files.
     */
    private final String uploadDir;

    /** Instantiates a Body parser with a configurable upload directory.
     *
     * <pre>
     *      Yoke yoke = new Yoke(...);
     *      yoke.use(new BodyParser("/upload"));
     * </pre>
     *
     * @param uploadDir upload directory path
     */
    public BodyParser(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    /** Instantiates a Body parser using the system default temp directory.
     *
     * <pre>
     *      Yoke yoke = new Yoke(...);
     *      yoke.use(new BodyParser());
     * </pre>
     */
    public BodyParser() {
        this(System.getProperty("java.io.tmpdir"));
    }

    /** Internal method to parse JSON requests directly to the YokeRequest object
     *
     * @param request http yoke request
     * @param buffer buffer containing the json payload
     * @param next middleware to be called next
     */
    private void parseJson(final YokeRequest request, final Buffer buffer, final Handler<Object> next) {
        String content = buffer.toString();
        if (content.isEmpty()) {
            next.handle(400);
            return;
        }

        try {
            request.setBody(JSON.decode(content));
        } catch (DecodeException e) {
            next.handle(e);
            return;
        }
        next.handle(null);
    }

    private boolean hasNoBody(YokeRequest request){
        final String method = request.method();
        if ("GET".equals(method) || "HEAD".equals(method)) return true;

        MultiMap headers = request.headers();
        if (!headers.contains("transfer-encoding") && !headers.contains("content-length")) return true;

        return false;
    }

    /** Handler for the parser. When the request method is GET or HEAD this is a Noop middleware.
     * If not the middleware verifies if there is a body and according to its headers tries to
     * parse it as JSON, form data or multi part upload.
     *
     * @param request http yoke request
     * @param next middleware to be called next
     */
    @Override
    public void handle(final YokeRequest request, final Handler<Object> next) {
        if(hasNoBody(request)){
            next.handle(null);
            return;
        }

        final ContentType type = ContentType.parse(request);
        final Buffer buffer = type.hasBuffer() ? new Buffer(0) : null;

        // enable the parsing at Vert.x level
        request.expectMultiPart(true);

        if (type == ContentType.MULTIPART) {
            request.uploadHandler(uploadHandler(request, next));
        }

        if(buffer != null){
            request.dataHandler(dataHandler(request, next, buffer));
        }

        request.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _void) {
                if (type == ContentType.JSON) {
                    parseJson(request, buffer, next);
                    return;
                }

                if(type == ContentType.OTHER){
                    request.setBody(buffer);
                }
                next.handle(null);
            }
        });
    }

    private Handler<HttpServerFileUpload> uploadHandler(final YokeRequest request, final Handler<Object> next) {
        return new Handler<HttpServerFileUpload>() {
            @Override
            public void handle(final HttpServerFileUpload fileUpload) {
                if (request.files() == null) {
                    request.setFiles(new HashMap<String, YokeFileUpload>());
                }
                YokeFileUpload upload = new YokeFileUpload(vertx, fileUpload, uploadDir);

                // setup callbacks
                fileUpload.exceptionHandler(new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable throwable) {
                        next.handle(throwable);
                    }
                });

                // stream to the generated path
                fileUpload.streamToFileSystem(upload.path());
                // store a reference in the request
                request.files().put(fileUpload.name(), upload);
            }
        };
    }

    private Handler<Buffer> dataHandler(final YokeRequest request, final Handler<Object> next, final Buffer buffer) {
        return new Handler<Buffer>() {
            long size = 0;
            final long limit = request.bodyLengthLimit();

            @Override
            public void handle(Buffer event) {
                if (limit == -1) {
                    buffer.appendBuffer(event);
                    return;
                }

                size += event.length();
                if (size < limit) {
                    buffer.appendBuffer(event);
                } else {
                    request.dataHandler(null);
                    request.endHandler(null);
                    next.handle(413);
                }
            }
        };
    }

    private enum ContentType {
        JSON,
        MULTIPART,
        URLENCODEC,
        OTHER;

        public static ContentType parse(YokeRequest request) {
            String type = request.getHeader("content-type");
            if(type == null) return OTHER;
            String lowerType = type.toLowerCase();
            if(lowerType.startsWith("application/json")) return JSON;
            if(lowerType.startsWith(HttpHeaders.Values.MULTIPART_FORM_DATA)) return MULTIPART;
            if(lowerType.startsWith(HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED)) return URLENCODEC;
            return OTHER;
        }

        public boolean hasBuffer(){
            return this != MULTIPART && this != URLENCODEC;
        }
    }
}
