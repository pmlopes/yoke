// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.middleware
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.core.JSON;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.json.DecodeException;

import java.util.HashMap;

// # BodyParser
//
// Parse request bodies, supports *application/json*, *application/x-www-form-urlencoded*, and *multipart/form-data*.
//
// Once data has been parsed the result is visible in the field ```body``` of the request. To help there are 2 helper
// getters for this field:  ```bodyJson()``` returns ```JsonObject``` and ```bodyBuffer()``` returns ```Buffer```.
//
// If the content type was *multipart/form-data* and there were uploaded files the files are ```files()``` returns
// ```Map<String, HttpServerFileUpload>```.
//
// ### Limitations
//
// Currently when parsing *multipart/form-data* if there are several files uploaded under the same name, only the last
// is preserved.
public class BodyParser extends Middleware {

    // Location on the file system to store the uploaded files.
    // @property uploadDir
    // @private
    private final String uploadDir;

    // Instantiates a Body parser with a configurable upload directory.
    //
    // @constructor
    // @param {String} uploadDir
    //
    // @example
    //      Yoke yoke = new Yoke(...);
    //      yoke.use(new BodyParser("/upload"));
    public BodyParser(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    // Instantiates a Body parser using the system default temp directory.
    //
    // @constructor
    //
    // @example
    //      Yoke yoke = new Yoke(...);
    //      yoke.use(new BodyParser());
    public BodyParser() {
        this(System.getProperty("java.io.tmpdir"));
    }

    // Internal method to parse JSON requests directly to the YokeRequest object
    //
    // @method parseJSON
    // @private
    // @asynchronous
    // @param {YokeRequest} request
    // @param {Buffer} buffer
    // @param {Handler} next
    private void parseJson(final YokeRequest request, final Buffer buffer, final Handler<Object> next) {
        try {
            String content = buffer.toString();
            if (content.length() > 0) {
                try {
                    request.setBody(JSON.decode(content));
                } catch (DecodeException e) {
                    next.handle(400);
                    return;
                }
                next.handle(null);
            } else {
                next.handle(400);
            }
        } catch (DecodeException ex) {
            next.handle(ex);
        }
    }

    // Handler for the parser. When the request method is GET or HEAD this is a Noop middleware.
    // If not the middleware verifies if there is a body and according to its headers tries to
    // parse it as JSON, form data or multi part upload.
    //
    // @method handle
    // @asynchronous
    // @param {YokeRequest} request
    // @param {Handler} next
    @Override
    public void handle(final YokeRequest request, final Handler<Object> next) {
        final String method = request.method();

        // GET and HEAD have no setBody
        if ("GET".equals(method) || "HEAD".equals(method)) {
            next.handle(null);
        } else {

            // has no body
            MultiMap headers = request.headers();
            if (!headers.contains("transfer-encoding") && !headers.contains("content-length")) {
                next.handle(null);
                return;
            }

            final String contentType = request.getHeader("content-type");

            final boolean isJSON = contentType != null && contentType.contains("application/json");
            final boolean isMULTIPART = contentType != null && contentType.contains("multipart/form-data");
            final boolean isURLENCODEC = contentType != null && contentType.contains("application/x-www-form-urlencoded");
            final Buffer buffer = (!isMULTIPART && !isURLENCODEC) ? new Buffer(0) : null;

            // enable the parsing at Vert.x level
            request.expectMultiPart(true);

            if (isMULTIPART) {
                request.uploadHandler(new Handler<HttpServerFileUpload>() {
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
                });
            }

            request.dataHandler(new Handler<Buffer>() {
                long size = 0;
                final long limit = request.bodyLengthLimit();

                @Override
                public void handle(Buffer event) {
                    if (limit != -1) {
                        size += event.length();
                        if (size < limit) {
                            if (!isMULTIPART && !isURLENCODEC) {
                                buffer.appendBuffer(event);
                            }
                        } else {
                            request.dataHandler(null);
                            request.endHandler(null);
                            next.handle(413);
                        }
                    } else {
                        if (!isMULTIPART && !isURLENCODEC) {
                            buffer.appendBuffer(event);
                        }
                    }
                }
            });

            request.endHandler(new Handler<Void>() {
                @Override
                public void handle(Void _void) {
                    if (isJSON) {
                        parseJson(request, buffer, next);
                    } else {
                        next.handle(null);
                    }
                }
            });
        }
    }
}
