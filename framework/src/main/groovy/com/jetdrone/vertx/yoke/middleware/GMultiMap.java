package com.jetdrone.vertx.yoke.middleware;

import org.vertx.java.core.MultiMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GMultiMap implements MultiMap {

    private final MultiMap impl;

    GMultiMap(MultiMap impl) {
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

    public void putAt(String key, Object value) {
        if (value != null) {
            if (value instanceof Iterable) {
                impl.add(key, (Iterable) value);
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

    @Override
    public List<Map.Entry<String, String>> entries() {
        return impl.entries();
    }

    @Override
    public boolean contains(String name) {
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
    public MultiMap add(String name, String value) {
        return impl.add(name, value);
    }

    @Override
    public MultiMap add(String name, Iterable<String> values) {
        return impl.add(name, values);
    }

    @Override
    public MultiMap add(MultiMap headers) {
        return impl.add(headers);
    }

    @Override
    public MultiMap add(Map<String, String> headers) {
        return impl.add(headers);
    }

    @Override
    public MultiMap set(String name, String value) {
        return impl.set(name, value);
    }

    @Override
    public MultiMap set(String name, Iterable<String> values) {
        return impl.set(name, values);
    }

    @Override
    public MultiMap set(MultiMap headers) {
        return impl.set(headers);
    }

    @Override
    public MultiMap set(Map<String, String> headers) {
        return impl.set(headers);
    }

    @Override
    public MultiMap remove(String name) {
        return impl.remove(name);
    }

    @Override
    public MultiMap clear() {
        return impl.clear();
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
