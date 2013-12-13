package com.jetdrone.vertx.yoke;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class Context implements Map<String, Object> {

    private final Map<String, Object> ro;
    private Map<String, Object> rw;

    public Context(Map<String, Object> ro) {
        this.ro = ro;
    }

    @Override
    public int size() {
        return ro.size() + (rw == null ? 0 : rw.size());
    }

    @Override
    public boolean isEmpty() {
        return ro.isEmpty() || rw == null || rw.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        if (rw != null) {
            if (rw.containsKey(o)) {
                return true;
            }
        }

        return ro.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        if (rw != null) {
            if (rw.containsValue(o)) {
                return true;
            }
        }

        return ro.containsValue(o);
    }

    @Override
    public Object get(Object o) {
        if (rw != null) {
            if (rw.containsKey(o)) {
                return rw.get(o);
            }
        }

        return ro.get(o);
    }

    @Override
    public Object put(String s, Object o) {
        if (rw == null) {
            rw = new HashMap<>();
        }
        return rw.put(s, o);
    }

    @Override
    public Object remove(Object o) {
        if (rw == null) {
            return null;
        }
        return rw.remove(o);
    }

    @Override
    public void putAll(Map<? extends String, ?> map) {
        if (rw == null) {
            rw = new HashMap<>();
        }
        rw.putAll(map);
    }

    @Override
    public void clear() {
        if (rw != null) {
            rw.clear();
        }
    }

    @Override
    public Set<String> keySet() {
        if (rw != null) {
            Set<String> keys = rw.keySet();
            keys.addAll(ro.keySet());
            return keys;
        }

        return ro.keySet();
    }

    @Override
    public Collection<Object> values() {
        if (rw != null) {
            Collection<Object> values = rw.values();
            values.addAll(ro.values());
            return values;
        }

        return ro.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        if (rw != null) {
            Set<Entry<String, Object>> entries = rw.entrySet();
            entries.addAll(ro.entrySet());
            return entries;
        }

        return ro.entrySet();
    }
}
