/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.engine;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.core.impl.LRUCache;
import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;

/**
 * # AbstractEngine
 *
 * Engine represents a Template Engine that can be registered with Yoke. Any template engine just needs to
 * extend this abstract class. The class provides access to the Vertx object so the engine might do I/O
 * operations in the context of the module.
 */
public abstract class AbstractEngine<T> implements Engine {

    protected Vertx vertx;

    private final LRUCache<String, T> cache = new LRUCache<>(1024);

    @Override
    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public String contentType() {
        return "text/html";
    }

    @Override
    public String contentEncoding() {
        return "UTF-8";
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
                    final long lastModified = asyncResult.result().lastModifiedTime();

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

    private void loadToCache(final String filename, final Handler<Throwable> next) {
        final FileSystem fileSystem = vertx.fileSystem();

        fileSystem.props(filename, new AsyncResultHandler<FileProps>() {
            @Override
            public void handle(AsyncResult<FileProps> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(asyncResult.cause());
                } else {
                    final long lastModified = asyncResult.result().lastModifiedTime();
                    // load from the file system
                    fileSystem.readFile(filename, new AsyncResultHandler<Buffer>() {
                        @Override
                        public void handle(AsyncResult<Buffer> asyncResult) {
                            if (asyncResult.failed()) {
                                next.handle(asyncResult.cause());
                            } else {
                                // cache the result
                                String result = asyncResult.result().toString(contentEncoding());
                                cache.put(filename, new LRUCache.CacheEntry<String, T>(lastModified, result));
                                next.handle(null);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Loads a resource from the filesystem into a string.
     *
     * Verifies if the file last modified date is newer than on the cache
     * if it is loads into a string
     * returns the string or the cached value
     *
     * @param filename File to read
     * @param handler Async handler
     */
    public void read(final String filename, final AsyncResultHandler<String> handler) {
        isFresh(filename, new Handler<Boolean>() {
            @Override
            public void handle(Boolean fresh) {
                if (fresh) {
                    String cachedValue = getFileFromCache(filename);
                    if (cachedValue != null) {
                        handler.handle(new YokeAsyncResult<>(null, cachedValue));
                        return;
                    }
                }
                // either fresh is false or cachedValue is null
                loadToCache(filename, new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable error) {
                        if (error != null) {
                            handler.handle(new YokeAsyncResult<String>(error, null));
                            return;
                        }
                        // no error
                        handler.handle(new YokeAsyncResult<>(null, getFileFromCache(filename)));
                    }
                });
            }
        });
    }

    /**
     * Returns the last modified time for the cache entry
     *
     * @param filename File to look for
     * @return last modified time in millis
     */
    public long lastModified(final String filename) {
        LRUCache.CacheEntry<String, T> cacheEntry = cache.get(filename);
        return cacheEntry.lastModified;
    }

    /**
     * Gets the content of the file from cache this is a synchronous operation since there is no blocking or I/O
     */
    private String getFileFromCache(String filename) {
        return cache.get(filename).raw;
    }

    /**
     * Gets the compiled value from cache this is a synchronous operation since there is no blocking or I/O
     */
    public T getTemplateFromCache(String filename) {
    	
    	LRUCache.CacheEntry<String, T> cachedTemplate = cache.get(filename);
    	
    	// this is to avoid null pointer exception in case of the layout composite template
    	if (cachedTemplate == null) return null;
    	
        return cachedTemplate.compiled;
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
}
