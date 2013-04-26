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
package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.middleware.YokeHttpServerRequest;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.file.FileProps;
import org.vertx.java.core.file.FileSystem;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Engine represents a Template Engine that can be registered with Yoke. Any template engine just needs to
 * extend this abstract class. The class provides access to the Vertx object so the engine might do I/O
 * operations in the context of the module.
 */
public abstract class Engine {

    protected Vertx vertx;
    private String contentType = "text/html;charset=UTF-8";

    protected void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public static class EngineAsyncResult<T> implements AsyncResult<T> {

        final Throwable throwable;
        final T result;

        public EngineAsyncResult(Throwable throwable, T result) {
            this.throwable = throwable;
            this.result = result;
        }

        @Override
        public T result() {
            return result;
        }

        @Override
        public Throwable cause() {
            return throwable;
        }

        @Override
        public boolean succeeded() {
            return throwable == null;
        }

        @Override
        public boolean failed() {
            return throwable != null;
        }
    }

    private static class FileCacheEntry {

        final long lastModified;
        final String body;

        FileCacheEntry(Date lastModified, String body) {
            this.lastModified = lastModified.getTime();
            this.body = body;
        }

        boolean isFresh(Date newDate) {
            return newDate.getTime() <= lastModified;
        }
    }

    private class LruCache extends LinkedHashMap<String, FileCacheEntry> {
        private final int maxEntries;

        public LruCache(final int maxEntries) {
            super(maxEntries + 1, 1.0f, true);
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<String, FileCacheEntry> eldest) {
            return super.size() > maxEntries;
        }
    }

    private final LruCache cache = new LruCache(1024);

    /**
     * Verifies if a file exists in the file system, exceptions are handled as not exists
     *
     * @param file File to look for
     * @param next next asynchronous handler
     */
    public void exists(final String file, final Handler<Boolean> next) {
        final FileSystem fileSystem = vertx.fileSystem();

        fileSystem.exists(file, new AsyncResultHandler<Boolean>() {
            @Override
            public void handle(AsyncResult<Boolean> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(false);
                } else {
                    next.handle(asyncResult.result());
                }
            }
        });
    }

    /**
     * Loads a file from the filesystem into a string.
     *
     * @param file File to load
     * @param next asynchronous handler
     */
    public void loadTemplate(final String file, final AsyncResultHandler<String> next) {
        final FileSystem fileSystem = vertx.fileSystem();

        fileSystem.props(file, new AsyncResultHandler<FileProps>() {
            @Override
            public void handle(AsyncResult<FileProps> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(new EngineAsyncResult<String>(asyncResult.cause(), null));
                } else {
                    FileCacheEntry cacheEntry = cache.get(file);
                    final Date lastModified = asyncResult.result().lastModifiedTime();

                    if (cacheEntry != null && cacheEntry.isFresh(lastModified)) {
                        next.handle(new EngineAsyncResult<>(null, cacheEntry.body));
                    } else {
                        // purge the cache
                        cache.remove(file);
                        // load from the file system
                        fileSystem.readFile(file, new AsyncResultHandler<Buffer>() {
                            @Override
                            public void handle(AsyncResult<Buffer> asyncResult) {
                                if (asyncResult.failed()) {
                                    next.handle(new EngineAsyncResult<String>(asyncResult.cause(), null));
                                } else {
                                    String body = asyncResult.result().toString("UTF-8");
                                    // save to the cache
                                    cache.put(file, new FileCacheEntry(lastModified, body));
                                    next.handle(new EngineAsyncResult<>(null, body));
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * The required to implement method.
     *
     * @param template - String representing the file path to the template
     * @param context - Map with key values that might get substituted in the template
     * @param asyncResultHandler - The future result handler with a Buffer in case of success
     */
    public abstract void render(final String template, final Map<String, Object> context, final AsyncResultHandler<Buffer> asyncResultHandler);
}
