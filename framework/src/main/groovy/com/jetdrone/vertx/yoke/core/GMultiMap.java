/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.core;

import org.vertx.groovy.core.MultiMap;
import org.vertx.groovy.core.impl.DefaultMultiMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GMultiMap implements MultiMap {

    private final MultiMap impl;

    public GMultiMap(org.vertx.java.core.MultiMap base) {
        this.impl = new DefaultMultiMap(base);
    }

    public Object getAt(String key) {
        List<String> out = impl.getAll(key);
        if (out != null) {
            if (out.size() == 1) {
                return out.get(0);
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public void putAt(String key, Object value) {
        if (value != null) {
            if (value instanceof Iterable) {
                impl.add(key, (Iterable<String>) value);
            } else {
                impl.add(key, (String) value);
            }
        }
    }

    @Override
    public String get(String name) {
        return impl.get(name);
    }

    @Override
    public List<String> getAll(String name) {
        return impl.getAll(name);
    }

    /**
     * Returns all entries it contains.
     *
     * @return A immutable {@link java.util.List} of the name-value entries, which will be
     *         empty if no pairs are found
     */
    @Override
    public List<Map.Entry<String, String>> getEntries() {
        return impl.getEntries();
    }

    @Override
    public boolean contains(String name) {
        return impl.contains(name);
    }

    @Override
    public boolean isEmpty() {
        return impl.isEmpty();
    }

    /**
     * Gets a immutable {@link java.util.Set} of all names
     *
     * @return A {@link java.util.Set} of all names
     */
    @Override
    public Set<String> getNames() {
        return impl.getNames();
    }

    @Override
    public GMultiMap add(String name, String value) {
        impl.add(name, value);
        return this;
    }

    @Override
    public GMultiMap add(String name, Iterable<String> values) {
        impl.add(name, values);
        return this;
    }

    @Override
    public GMultiMap set(String name, String value) {
        impl.set(name, value);
        return this;
    }

    @Override
    public GMultiMap set(String name, Iterable<String> values) {
        impl.set(name, values);
        return this;
    }

    @Override
    public GMultiMap set(MultiMap headers) {
        impl.set(headers);
        return this;
    }

    @Override
    public GMultiMap set(Map<String, String> headers) {
        impl.set(headers);
        return this;
    }

    @Override
    public GMultiMap remove(String name) {
        impl.remove(name);
        return this;
    }

    @Override
    public GMultiMap clear() {
        impl.clear();
        return this;
    }

    /**
     * Return the number of names.
     */
    @Override
    public int getSize() {
        return impl.getSize();
    }

    /**
     * Same as {@link #set(java.lang.String, java.lang.String)}  or {@link #set(java.lang.String, java.lang.Iterable)}
     */
    @Override
    public GMultiMap leftShift(Map.Entry<String, ?> entry) {
        impl.leftShift(entry);
        return this;
    }

    /**
     * Same as {@link #set(org.vertx.groovy.core.MultiMap)}
     */
    @Override
    public GMultiMap leftShift(MultiMap map) {
        impl.leftShift(map);
        return this;
    }

    @Override
    public Iterator iterator() {
        return impl.iterator();
    }
}
