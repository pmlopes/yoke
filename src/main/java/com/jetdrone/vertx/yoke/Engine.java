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

import com.jetdrone.vertx.yoke.util.LRUCache;
import com.jetdrone.vertx.yoke.util.YokeAsyncResult;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.file.FileProps;
import org.vertx.java.core.file.FileSystem;

import java.util.Date;
import java.util.Map;

/**
 * Engine represents a Template Engine that can be registered with Yoke. Any template engine just needs to
 * extend this abstract class. The class provides access to the Vertx object so the engine might do I/O
 * operations in the context of the module.
 */
public abstract class Engine<T> {

    protected Vertx vertx;
    private String contentType = "text/html;charset=UTF-8";

    private final LRUCache<String, T> cache = new LRUCache<>(1024);

    protected void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void exists(String filename, final Handler<Boolean> next) {
        final FileSystem fileSystem = vertx.fileSystem();

        fileSystem.exists(filename, new AsyncResultHandler<Boolean>() {
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
     * Verifies if a file in the filesystem is still fresh against the cache. Errors are treated as not fresh.
     *
     * @param filename File to look for
     * @param next next asynchronous handler
     */
    public void isFresh(final String filename, final Handler<Boolean> next) {
        final FileSystem fileSystem = vertx.fileSystem();

        fileSystem.props(filename, new AsyncResultHandler<FileProps>() {
            @Override
            public void handle(AsyncResult<FileProps> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(false);
                } else {
                    LRUCache.CacheEntry<String, T> cacheEntry = cache.get(filename);
                    final Date lastModified = asyncResult.result().lastModifiedTime();

                    if (cacheEntry == null) {
                        next.handle(false);
                    } else {
                        if (cacheEntry.isFresh(lastModified)) {
                            next.handle(true);
                        } else {
                            // not fresh anymore, purge it
                            cache.remove(filename);
                            next.handle(false);
                        }
                    }
                }
            }
        });
    }

    public void load(final String filename, final Handler<AsyncResult<String>> next) {
        final FileSystem fileSystem = vertx.fileSystem();

        fileSystem.props(filename, new AsyncResultHandler<FileProps>() {
            @Override
            public void handle(AsyncResult<FileProps> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(new YokeAsyncResult<String>(asyncResult.cause()));
                } else {
                    final Date lastModified = asyncResult.result().lastModifiedTime();
                    // load from the file system
                    fileSystem.readFile(filename, new AsyncResultHandler<Buffer>() {
                        @Override
                        public void handle(AsyncResult<Buffer> asyncResult) {
                            if (asyncResult.failed()) {
                                next.handle(new YokeAsyncResult<String>(asyncResult.cause()));
                            } else {
                                // cache the result
                                String result = asyncResult.result().toString();
                                cache.put(filename, new LRUCache.CacheEntry<String, T>(lastModified, result));
                                next.handle(new YokeAsyncResult<String>(null, result));
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Gets the content of the file from cache this is a synchronous operation since there is no blocking or I/O
     */
    public String getFileFromCache(String filename) {
        return cache.get(filename).raw;
    }

    /**
     * Gets the compiled value from cache this is a synchronous operation since there is no blocking or I/O
     */
    public T getTemplateFromCache(String filename) {
        return cache.get(filename).compiled;
    }

    /**
     * Gets the compiled value from cache this is a synchronous operation since there is no blocking or I/O
     */
    public void putTemplateToCache(String filename, T template) {
        cache.putCompiled(filename, template);
    }

    /**
     * Removes an entry from cache
     */
    public void removeFromCache(String filename) {
        cache.remove(filename);
    }

    /**
     * The required to implement method.
     *
     * @param filename - String representing the file path to the template
     * @param context - Map with key values that might get substituted in the template
     * @param handler - The future result handler with a Buffer in case of success
     */
    public abstract void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<String>> handler);
}
