/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.core.impl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Double entry LRUCache
 * @param <R> Raw value (raw value is immutable)
 * @param <C> Compiled value
 */
public class LRUCache<R, C> extends LinkedHashMap<String, LRUCache.CacheEntry<R, C>> {

    private static final long serialVersionUID = 1l;

    /**
     * Generic cache entry.
     */
    public static class CacheEntry<R, C> {

        public final long lastModified;
        public final R raw;
        public C compiled;

        public CacheEntry(long lastModified, R raw, C compiled) {
            this.lastModified = lastModified;
            this.raw = raw;
            this.compiled = compiled;
        }

        public CacheEntry(long lastModified, R raw) {
            this(lastModified, raw, null);
        }

        public boolean isFresh(long newDate) {
            return newDate <= lastModified;
        }
    }

    private final int maxEntries;

    public LRUCache(final int maxEntries) {
        super(maxEntries + 1, 1.0f, true);
        this.maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<String, CacheEntry<R, C>> eldest) {
        return super.size() > maxEntries;
    }

    public void putCompiled(String key, C compiled) {
        CacheEntry<R, C> original = get(key);
        original.compiled = compiled;
    }
}
