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

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.HashMap;

public class BodyParser extends Middleware {

    private final String uploadDir;

    public BodyParser(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public BodyParser() {
        this(System.getProperty("java.io.tmpdir"));
    }

    private void parseJson(final YokeRequest request, final Buffer buffer, final Handler<Object> next) {
        try {
            String jsonString = buffer.toString();
            if (jsonString.length() > 0) {
                switch (jsonString.charAt(0)) {
                    case '{':
                        request.setBody(new JsonObject(jsonString));
                        next.handle(null);
                        break;
                    case '[':
                        request.setBody(new JsonArray(jsonString));
                        next.handle(null);
                        break;
                    default:
                        next.handle(400);
                }
            } else {
                next.handle(400);
            }
        } catch (DecodeException ex) {
            next.handle(ex);
        }
    }

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

            if (isMULTIPART || isURLENCODEC) {
                // enable the parsing at Vert.x level
                request.expectMultiPart(true);
            }

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
