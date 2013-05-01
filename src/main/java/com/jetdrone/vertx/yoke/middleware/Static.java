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
import com.jetdrone.vertx.yoke.MimeType;
import com.jetdrone.vertx.yoke.util.Utils;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.file.FileProps;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.json.JsonArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Static extends Middleware {

    static final SimpleDateFormat ISODATE;
    private final String directoryTemplate;

    static {
        ISODATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS zzz");
        ISODATE.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final String root;
    private final long maxAge;
    private final boolean directoryListing;
    private final boolean includeHidden;

    public Static(String root, long maxAge, boolean directoryListing, boolean includeHidden) {
        if (root.endsWith("/")) {
            root = root.substring(0, root.length() - 1);
        }
        this.root = root;
        this.maxAge = maxAge;
        this.includeHidden = includeHidden;
        this.directoryListing = directoryListing;
        this.directoryTemplate = Utils.readResourceToBuffer(getClass(), "directory.html").toString();
    }

    public Static(String root,long maxAge) {
        this(root, maxAge, false, false);
    }

    public Static(String root) {
        this(root, 86400000, false, false);
    }

    private void writeHeaders(final YokeHttpServerRequest request, final FileProps props) {

        MultiMap headers = request.response().headers();

        if (!headers.contains("etag")) {
            headers.set("etag", "\"" + props.size() + "-" + props.lastModifiedTime().getTime() + "\"");
        }

        if (!headers.contains("date")) {
            headers.set("date", ISODATE.format(new Date()));
        }

        if (!headers.contains("cache-control")) {
            headers.set("cache-control", "public, max-age=" + maxAge / 1000);
        }

        if (!headers.contains("last-modified")) {
            headers.set("last-modified", ISODATE.format(props.lastModifiedTime()));
        }
    }

    private void sendFile(final YokeHttpServerRequest request, final String file, final FileProps props) {
        // write content type
        String contentType = MimeType.getMime(file);
        String charset = MimeType.getCharset(contentType);
        request.response().putHeader("content-type",contentType + (charset != null ? "; charset=" + charset : ""));
        request.response().putHeader("Content-Length", Long.toString(props.size()));

        // head support
        if ("HEAD".equals(request.method())) {
            request.response().end();
        } else {
            request.response().sendFile(file);
        }
    }

    private void sendDirectory(final YokeHttpServerRequest request, final String dir, final Handler<Object> next) {
        final FileSystem fileSystem = vertx.fileSystem();

        fileSystem.readDir(dir, new AsyncResultHandler<String[]>() {
            @Override
            public void handle(AsyncResult<String[]> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(asyncResult.cause());
                } else {
                    String accept = request.getHeader("accept", "text/plain");

                    if (accept.contains("html")) {
                        String normalizedDir = dir.substring(root.length());
                        if (!normalizedDir.endsWith("/")) {
                            normalizedDir += "/";
                        }

                        String file;
                        StringBuilder files = new StringBuilder("<ul id=\"files\">");

                        for (String s : asyncResult.result()) {
                            file = s.substring(s.lastIndexOf('/') + 1);
                            // skip dot files
                            if (!includeHidden && file.charAt(0) == '.') {
                                continue;
                            }
                            files.append("<li><a href=\"");
                            files.append(normalizedDir);
                            files.append(file);
                            files.append("\" title=\"");
                            files.append(file);
                            files.append("\">");
                            files.append(file);
                            files.append("</a></li>");
                        }

                        files.append("</ul>");

                        StringBuilder directory = new StringBuilder();
                        // define access to root
                        directory.append("<a href=\"/\">/</a> ");

                        StringBuilder expandingPath = new StringBuilder();
                        String[] dirParts = normalizedDir.split("/");
                        for (int i = 1; i < dirParts.length; i++) {
                            // dynamic expansion
                            expandingPath.append("/");
                            expandingPath.append(dirParts[i]);
                            // anchor building
                            if (i > 1) {
                                directory.append(" / ");
                            }
                            directory.append("<a href=\"");
                            directory.append(expandingPath.toString());
                            directory.append("\">");
                            directory.append(dirParts[i]);
                            directory.append("</a>");
                        }

                        request.response().putHeader("Content-Type", "text/html");
                        request.response().end(
                                directoryTemplate.replace("{title}", (String) request.get("title")).replace("{directory}", normalizedDir)
                                        .replace("{linked-path}", directory.toString())
                                        .replace("{files}", files.toString()));
                    } else if (accept.contains("json")) {
                        String file;
                        JsonArray json = new JsonArray();

                        for (String s : asyncResult.result()) {
                            file = s.substring(s.lastIndexOf('/') + 1);
                            // skip dot files
                            if (!includeHidden && file.charAt(0) == '.') {
                                continue;
                            }
                            json.addString(file);
                        }

                        request.response().end(json);
                    } else {
                        String file;
                        StringBuilder buffer = new StringBuilder();

                        for (String s : asyncResult.result()) {
                            file = s.substring(s.lastIndexOf('/') + 1);
                            // skip dot files
                            if (!includeHidden && file.charAt(0) == '.') {
                                continue;
                            }
                            buffer.append(file);
                            buffer.append('\n');
                        }

                        request.response().putHeader("content-type", "text/plain");
                        request.response().end(buffer.toString());
                    }
                }
            }
        });
    }

    private boolean isFresh(final YokeHttpServerRequest request) {
        // defaults
        boolean etagMatches = true;
        boolean notModified = true;

        // fields
        String modifiedSince = request.getHeader("if-modified-since");
        String noneMatch = request.getHeader("if-none-match");
        String[] noneMatchTokens = null;
        String lastModified = request.response().getHeader("last-modified");
        String etag = request.response().getHeader("etag");

        // unconditional request
        if (modifiedSince == null && noneMatch == null) {
            return false;
        }

        // parse if-none-match
        if (noneMatch != null) {
            noneMatchTokens = noneMatch.split(" *, *");
        }

        // if-none-match
        if (noneMatchTokens != null) {
            etagMatches = false;
            for (String s : noneMatchTokens) {
                if (etag.equals(s) || "*".equals(noneMatchTokens[0])) {
                    etagMatches = true;
                    break;
                }
            }
        }

        // if-modified-since
        if (modifiedSince != null) {
            try {
                Date modifiedSinceDate = ISODATE.parse(modifiedSince);
                Date lastModifiedDate = ISODATE.parse(lastModified);
                notModified = lastModifiedDate.getTime() <= modifiedSinceDate.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                notModified = false;
            }
        }

        return etagMatches && notModified;
    }

    @Override
    public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
        if (!"GET".equals(request.method()) && !"HEAD".equals(request.method())) {
            next.handle(null);
        } else {
            final String file = root + request.path();

            if (!includeHidden) {
                int idx = file.lastIndexOf('/');
                String name = file.substring(idx + 1);
                if (name.length() > 0 && name.charAt(0) == '.') {
                    next.handle(null);
                    return;
                }
            }

            final FileSystem fileSystem = vertx.fileSystem();

            fileSystem.exists(file, new AsyncResultHandler<Boolean>() {
                @Override
                public void handle(AsyncResult<Boolean> asyncResult) {
                    if (asyncResult.failed()) {
                        next.handle(asyncResult.cause());
                    } else {
                        if (!asyncResult.result()) {
                            // no static file found, let the next middleware handle it
                            next.handle(null);
                        } else {
                            fileSystem.props(file, new AsyncResultHandler<FileProps>() {
                                @Override
                                public void handle(AsyncResult<FileProps> props) {
                                    if (props.failed()) {
                                        next.handle(props.cause());
                                    } else {
                                        if (props.result().isDirectory()) {
                                            if (directoryListing) {
                                                // write cache control headers
                                                writeHeaders(request, props.result());
                                                // verify if we are still fresh
                                                if (isFresh(request)) {
                                                    request.response().setStatusCode(304);
                                                    request.response().end();
                                                } else {
                                                    sendDirectory(request, file, next);
                                                }
                                            } else {
                                                // we are not listing directories
                                                next.handle(null);
                                            }
                                        } else {
                                            // write cache control headers
                                            writeHeaders(request, props.result());
                                            // verify if we are still fresh
                                            if (isFresh(request)) {
                                                request.response().setStatusCode(304);
                                                request.response().end();
                                            } else {
                                                sendFile(request, file, props.result());
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
    }
}
