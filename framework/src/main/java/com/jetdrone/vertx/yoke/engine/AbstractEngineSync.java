/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.engine;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.core.impl.LRUCache;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.file.FileProps;
import org.vertx.java.core.file.FileSystem;

import java.util.Date;
import java.util.Map;

/**
 * # AbstractEngineSync
 *
 * Engine represents a Template Engine that can be registered with Yoke. Any template engine just needs to
 * extend this abstract class. The class provides access to the Vertx object so the engine might do I/O
 * operations in the context of the module.
 */
public abstract class AbstractEngineSync<T> implements Engine {

    public static final String DEFAULT_TEMPLATEBODY_KEY = "TemplateBody";

    protected Vertx vertx;

    private final LRUCache<String, T> cache = new LRUCache<>(1024);

    // The main placeholder text. For example, in Groovy engine, the layout template
    // should have ${TemplateBody} somewhere inside to get replaced with the real template raw content
    private final String templateBodyKey;

    public AbstractEngineSync() {
        this(DEFAULT_TEMPLATEBODY_KEY);
    }

    public AbstractEngineSync(String templateBodyKey) {
        this.templateBodyKey = templateBodyKey;
    }

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
     */
    public boolean isFresh(final String filename) {
        final FileSystem fileSystem = vertx.fileSystem();

        try {
            FileProps fileProps = fileSystem.propsSync(filename);
            LRUCache.CacheEntry<String, T> cacheEntry = cache.get(filename);
            final Date lastModified = fileProps.lastModifiedTime();

            if (cacheEntry == null) {
                return false;
            }
            if (cacheEntry.isFresh(lastModified)) {
                return true;
            }
            // not fresh anymore, purge it
            cache.remove(filename);
            return false;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Returns the last modified time for the cache entry
     *
     * @param filename File to look for
     */
    public long lastModified(final String filename) {
        LRUCache.CacheEntry<String, T> cacheEntry = cache.get(filename);
        if (cacheEntry == null) {
            return -1;
        }
        return cacheEntry.lastModified;
    }

    private void loadToCache(final String filename) {
        final FileSystem fileSystem = vertx.fileSystem();

        FileProps fileProps = fileSystem.propsSync(filename);
        final Date lastModified = fileProps.lastModifiedTime();
        // load from the file system
        Buffer content = fileSystem.readFileSync(filename);
        // cache the result
        cache.put(filename, new LRUCache.CacheEntry<String, T>(lastModified, content.toString(contentEncoding())));
    }

    /**
     * Loads a resource from the filesystem into a string.
     *
     * Verifies if the file last modified date is newer than on the cache
     * if it is loads into a string
     * returns the string or the cached value
     */
    public String read(final String filename) {
        if (isFresh(filename)) {
            String cachedValue = getFileFromCache(filename);
            if (cachedValue != null) {
                return cachedValue;
            }
        }
        // either fresh is false or cachedValue is null
        loadToCache(filename);
        return getFileFromCache(filename);
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

    /**
     * Default Implementation for render with layout.
     *
     * @param filename String representing the file path to the template
     * @param layoutFilename String representing the file path to the layout template
     * @param context  Map with key values that might get substituted in the template
     * @param handler  The future result handler with a Buffer in case of success
     */
    @Override
    public void render(final String filename, final String layoutFilename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> handler) {
        // this implementation just reuses the existing template so it is easier to add more render engines.
        render(filename, context, new Handler<AsyncResult<Buffer>>() {
            @Override
            public void handle(AsyncResult<Buffer> render) {
                if (render.failed()) {
                    handler.handle(render);
                    return;
                }

                // put the generated body into the context
                context.put(templateBodyKey, render.result());

                // we now have the body of the template, lets replace that now on the layout template
                render(layoutFilename, context, handler);
            }
        });
    }
}
