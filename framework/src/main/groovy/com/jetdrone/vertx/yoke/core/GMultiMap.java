package com.jetdrone.vertx.yoke.core;

import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GMultiMap implements MultiMap {

    private final MultiMap impl;

    public GMultiMap() {
        this.impl = new CaseInsensitiveMultiMap();
    }

    public GMultiMap(MultiMap impl) {
        this.impl = impl;
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
    public String get(CharSequence name) {
        return impl.get(name);
    }

    @Override
    public String get(String name) {
        return impl.get(name);
    }

    @Override
    public List<String> getAll(String name) {
        return impl.getAll(name);
    }

    @Override
    public List<String> getAll(CharSequence name) {
        return impl.getAll(name);
    }

    @Override
    public List<Map.Entry<String, String>> entries() {
        return impl.entries();
    }

    @Override
    public boolean contains(String name) {
        return impl.contains(name);
    }

    @Override
    public boolean contains(CharSequence name) {
        return impl.contains(name);
    }

    @Override
    public boolean isEmpty() {
        return impl.isEmpty();
    }

    @Override
    public Set<String> names() {
        return impl.names();
    }

    @Override
    public GMultiMap add(String name, String value) {
        impl.add(name, value);
        return this;
    }

    @Override
    public GMultiMap add(CharSequence name, CharSequence value) {
        impl.add(name, value);
        return this;
    }

    @Override
    public GMultiMap add(String name, Iterable<String> values) {
        impl.add(name, values);
        return this;
    }

    @Override
    public GMultiMap add(CharSequence name, Iterable<CharSequence> values) {
        impl.add(name, values);
        return this;
    }

    @Override
    public GMultiMap add(MultiMap headers) {
        impl.add(headers);
        return this;
    }

    @Override
    public GMultiMap add(Map<String, String> headers) {
        impl.add(headers);
        return this;
    }

    @Override
    public GMultiMap set(String name, String value) {
        impl.set(name, value);
        return this;
    }

    @Override
    public GMultiMap set(CharSequence name, CharSequence value) {
        impl.set(name, value);
        return this;
    }

    @Override
    public GMultiMap set(String name, Iterable<String> values) {
        impl.set(name, values);
        return this;
    }

    @Override
    public GMultiMap set(CharSequence name, Iterable<CharSequence> values) {
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
    public GMultiMap remove(CharSequence name) {
        impl.remove(name);
        return this;
    }

    @Override
    public GMultiMap clear() {
        impl.clear();
        return this;
    }

    @Override
    public int size() {
        return impl.size();
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return impl.iterator();
    }
}
