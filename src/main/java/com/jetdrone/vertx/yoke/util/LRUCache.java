package com.jetdrone.vertx.yoke.util;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Double entry LRUCache
 * @param <R> Raw value (raw value is immutable)
 * @param <C> Compiled value
 */
public class LRUCache<R, C> extends LinkedHashMap<String, LRUCache.CacheEntry<R, C>> {

    /**
     * Generic cache entry.
     */
    public static class CacheEntry<R, C> {

        public final long lastModified;
        public final R raw;
        public C compiled;

        public CacheEntry(Date lastModified, R raw, C compiled) {
            this.lastModified = lastModified.getTime();
            this.raw = raw;
            this.compiled = compiled;
        }

        public CacheEntry(Date lastModified, R raw) {
            this(lastModified, raw, null);
        }

        public boolean isFresh(Date newDate) {
            return newDate.getTime() <= lastModified;
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
