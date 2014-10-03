/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.AbstractMiddleware;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.core.JSON;
import com.jetdrone.vertx.yoke.core.YokeFileUpload;
import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.Handler;
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
 * If the content type was *multipart/form-data* and there were uploaded files the files are ```files()``` returns
 * `Map&lt;String, HttpServerFileUpload&gt;`.
 *
 * ### Limitations
 *
 * Currently when parsing *multipart/form-data* if there are several files uploaded under the same name, only the last
 * is preserved.
 */
public class BodyParser extends AbstractMiddleware {

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
    public BodyParser(@NotNull String uploadDir) {
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

    /** Handler for the parser. When the request method is GET or HEAD this is a Noop middleware.
     * If not the middleware verifies if there is a body and according to its headers tries to
     * parse it as JSON, form data or multi part upload.
     *
     * @param request http yoke request
     * @param next middleware to be called next
     */
    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
        final String method = request.method();

        // GET and HEAD have no setBody
        if ("GET".equals(method) || "HEAD".equals(method) || !request.hasBody()) {
            next.handle(null);
        } else {

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
                        final YokeFileUpload upload = new YokeFileUpload(vertx(), fileUpload, uploadDir);

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
                        // set up a callback to remove the file from the file system when the request completes
                        request.response().endHandler(new Handler<Void>() {
                            @Override
                            public void handle(Void event) {
                                if (upload.isTransient()) {
                                    upload.delete();
                                }
                            }
                        });
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

                            request.put("canceled", true);
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
                        if (buffer != null && buffer.length() > 0) {
                            try {
                                String content = buffer.toString();
                                request.setBody(JSON.decode(content));
                            } catch (DecodeException e) {
                                next.handle(400);
                                return;
                            }
                            if (!request.get("canceled", false)) {
                                next.handle(null);
                            }
                        } else if (buffer != null && buffer.length() == 0) {
                            // special case for IE and Safari than even for 0 content length, send content type header
                            if (request.contentLength() == 0) {
                                request.setBody(null);

                                if (!request.get("canceled", false)) {
                                    next.handle(null);
                                }
                            } else {
                                next.handle(400);
                            }
                        } else {
                            next.handle(400);
                        }
                    } else {
                        if (buffer != null) {
                            request.setBody(buffer);
                        }
                        if (!request.get("canceled", false)) {
                            next.handle(null);
                        }
                    }
                }
            });
        }
    }
}
