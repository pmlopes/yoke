package com.jetdrone.vertx.yoke.core.impl;

import com.jetdrone.vertx.yoke.middleware.YokeCookie;
import com.jetdrone.vertx.yoke.middleware.YokeFileUpload;
import org.mozilla.javascript.Scriptable;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.json.JsonElement;

import java.util.List;
import java.util.Map;
import java.util.Set;

class JSUtil {

    private JSUtil() {}

    static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    static Scriptable toScriptable(final MultiMap multiMap) {
        return new Scriptable() {

            private Scriptable prototype, parent;

            @Override
            public String getClassName() {
                return "JSMultiMap";
            }

            @Override
            public Object get(String name, Scriptable start) {
                List<String> items = multiMap.getAll(name);
                if (items.isEmpty()) {
                    return NOT_FOUND;
                }
                if (items.size() == 1) {
                    return items.get(0);
                }

                return toScriptable(items);
            }

            @Override
            public Object get(int index, Scriptable start) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean has(String name, Scriptable start) {
                return multiMap.contains(name);
            }

            @Override
            public boolean has(int index, Scriptable start) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void put(String name, Scriptable start, Object value) {
                multiMap.add(name, value != null ? value.toString() : null);
            }

            @Override
            public void put(int index, Scriptable start, Object value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void delete(String name) {
                multiMap.remove(name);
            }

            @Override
            public void delete(int index) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Scriptable getPrototype() {
                return prototype;
            }

            @Override
            public void setPrototype(Scriptable prototype) {
                this.prototype = prototype;
            }

            @Override
            public Scriptable getParentScope() {
                return parent;
            }

            @Override
            public void setParentScope(Scriptable parent) {
                this.parent = parent;
            }

            @Override
            public Object[] getIds() {
                return multiMap.names().toArray();
            }

            @Override
            public Object getDefaultValue(Class<?> hint) {
                return "[object JSMultiMap]";
            }

            @Override
            public boolean hasInstance(Scriptable instance) {
                Scriptable proto = instance.getPrototype();
                while (proto != null) {
                    if (proto.equals(this))
                        return true;
                    proto = proto.getPrototype();
                }

                return false;
            }
        };
    }

    static Scriptable toScriptable(final Set<?> set) {
        // TODO: implement me!!!
        return null;
    }

    static Scriptable toScriptable(final List<?> list) {
        // TODO: implement me!!!
        return null;
    }

    static Scriptable toScriptable(final YokeCookie cookie) {
        // TODO: implement me!!!
        return null;
    }

    static Scriptable toScriptable(final YokeFileUpload fileUpload) {
        // TODO: implement me!!!
        return null;
    }

    static Scriptable toScriptable(final Map<?, ?> map) {
        return new Scriptable() {

            private Scriptable prototype, parent;

            @Override
            public String getClassName() {
                return "JSMultiMap";
            }

            @Override
            public Object get(String name, Scriptable start) {
                if (map.containsKey(name)) {
                    // TODO: cast return type to js friendly type
                    return map.get(name);
                }

                return NOT_FOUND;
            }

            @Override
            public Object get(int index, Scriptable start) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean has(String name, Scriptable start) {
                return map.containsKey(name);
            }

            @Override
            public boolean has(int index, Scriptable start) {
                throw new UnsupportedOperationException();
            }

            @Override
            @SuppressWarnings("unchecked")
            public void put(String name, Scriptable start, Object value) {
                ((Map) map).put(name, value);
            }

            @Override
            public void put(int index, Scriptable start, Object value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void delete(String name) {
                map.remove(name);
            }

            @Override
            public void delete(int index) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Scriptable getPrototype() {
                return prototype;
            }

            @Override
            public void setPrototype(Scriptable prototype) {
                this.prototype = prototype;
            }

            @Override
            public Scriptable getParentScope() {
                return parent;
            }

            @Override
            public void setParentScope(Scriptable parent) {
                this.parent = parent;
            }

            @Override
            public Object[] getIds() {
                return map.keySet().toArray();
            }

            @Override
            public Object getDefaultValue(Class<?> hint) {
                return "[object JSMultiMap]";
            }

            @Override
            public boolean hasInstance(Scriptable instance) {
                Scriptable proto = instance.getPrototype();
                while (proto != null) {
                    if (proto.equals(this))
                        return true;
                    proto = proto.getPrototype();
                }

                return false;
            }
        };
    }

    static Scriptable toScriptable(final JsonElement json) {
        return new Scriptable() {

            private Scriptable prototype, parent;

            @Override
            public String getClassName() {
                return "JSJsonElement";
            }

            @Override
            public Object get(String name, Scriptable start) {
                if (json.isObject()) {
                    if (json.asObject().containsField(name)) {
                        // TODO: convert jsonelement/map/jsonarray/list
                        return json.asObject().getField(name);
                    } else {
                        return NOT_FOUND;
                    }
                } else {
                    return NOT_FOUND;
                }
            }

            @Override
            public Object get(int index, Scriptable start) {
                if (json.isArray()) {
                    if (index >= 0 && json.asArray().size() > index) {
                        // TODO: convert jsonelement/map/jsonarray/list
                        return json.asArray().get(index);
                    } else {
                        return NOT_FOUND;
                    }
                } else {
                    return NOT_FOUND;
                }
            }

            @Override
            public boolean has(String name, Scriptable start) {
                return json.isObject() && json.asObject().containsField(name);
            }

            @Override
            public boolean has(int index, Scriptable start) {
                return json.isArray() && index >= 0 && json.asArray().size() > index;
            }

            @Override
            public void put(String name, Scriptable start, Object value) {
                json.asObject().putValue(name, value);
            }

            @Override
            public void put(int index, Scriptable start, Object value) {
                throw new RuntimeException("JsonArray does not allow put at random index");
            }

            @Override
            public void delete(String name) {
                json.asObject().removeField(name);
            }

            @Override
            public void delete(int index) {
                throw new RuntimeException("JsonArray does not allow delete at random index");
            }

            @Override
            public Scriptable getPrototype() {
                return prototype;
            }

            @Override
            public void setPrototype(Scriptable prototype) {
                this.prototype = prototype;
            }

            @Override
            public Scriptable getParentScope() {
                return parent;
            }

            @Override
            public void setParentScope(Scriptable parent) {
                this.parent = parent;
            }

            @Override
            public Object[] getIds() {
                if (json.isObject()) {
                    return json.asObject().getFieldNames().toArray();
                }
                return EMPTY_OBJECT_ARRAY;
            }

            @Override
            public Object getDefaultValue(Class<?> hint) {
                return "[object JSJsonElement]";
            }

            @Override
            public boolean hasInstance(Scriptable instance) {
                return instance != null && instance instanceof JsonElement;
            }
        };
    }

    static boolean is(Object[] args, Class<?>... classes) {
        if (args == null && classes == null) {
            return true;
        }

        if (args == null || classes == null) {
            return false;
        }

        if (args.length < classes.length) {
            return false;
        }

        for (int i = 0; i < classes.length; i++) {
            if (args[i] != null && !classes[i].isInstance(args[i])) {
                return false;
            }
        }

        return true;
    }

    static boolean isVararg(Object[] args, Class<?> clazz) {
        if (args == null && clazz == null) {
            return true;
        }

        if (args == null || clazz == null) {
            return false;
        }

        for (Object arg : args) {
            if (arg != null && !clazz.isInstance(arg)) {
                return false;
            }
        }

        return true;
    }
}
