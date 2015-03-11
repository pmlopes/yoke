package com.jetdrone.vertx.yoke.core.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;

import com.jetdrone.vertx.yoke.store.json.ChangeAwareJsonArray;
import com.jetdrone.vertx.yoke.store.json.ChangeAwareJsonElement;

class JSUtil {

    private JSUtil() {}

    static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    static Object javaToJS(Object value, Scriptable scope) {
        if (value == null)
            return null;
        if (value instanceof MultiMap)
            return toScriptable((MultiMap) value, scope);
        if (value instanceof Set)
            return toScriptable((Set<?>) value, scope);
        if (value instanceof List)
            return toScriptable((List<?>) value, scope);
        if (value instanceof Map)
            return toScriptable((Map<?, ?>) value, scope);
        if (value instanceof JsonElement)
            return toScriptable((JsonElement) value, scope);
        if (value instanceof ChangeAwareJsonElement)
            return toScriptable((ChangeAwareJsonElement) value, scope);
        return Context.javaToJS(value, scope);
    }

    private static Scriptable toScriptable(final MultiMap multiMap, final Scriptable scope) {
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
                    return javaToJS(items.get(0), start);
                }

                return javaToJS(items, start);
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

    /**
     * Return a read-only {@link Scriptable} representing the given {@code set}.
     * 
     * @param set
     * @param scope
     * @return
     */
    static Scriptable toScriptable(final Set<?> set, final Scriptable scope) {
        final Object[] elements = set.toArray();
        final Object[] ids = new Object[elements.length];
        for (int i = 0; i < elements.length; ++i)
            ids[i] = i;
        
        return new Scriptable() {

            private Scriptable prototype, parent;

            @Override
            public String getClassName() {
                return "JSSet";
            }

            @Override
            public Object get(String name, Scriptable start) {
                switch (name) {
                case "length":
                    return elements.length;
                default:
                    return NOT_FOUND;
                }
            }

            @Override
            public Object get(int index, Scriptable start) {
                if (index < 0 || index >= elements.length)
                    return NOT_FOUND;
                return javaToJS(elements[index], scope);
            }

            @Override
            public boolean has(String name, Scriptable start) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean has(int index, Scriptable start) {
                return 0 <= index && index < elements.length;
            }

            @Override
            public void put(String name, Scriptable start, Object value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void put(int index, Scriptable start, Object value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void delete(String name) {
                throw new UnsupportedOperationException();
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
                return ids;
            }

            @Override
            public Object getDefaultValue(Class<?> hint) {
                return "[object JSSet]";
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

    static Scriptable toScriptable(final List<?> list, final Scriptable scope) {
        return new Scriptable() {

            private Scriptable prototype, parent;

            @Override
            public String getClassName() {
                return "JSList";
            }

            @Override
            public Object get(String name, Scriptable start) {
                switch (name) {
                case "length":
                    return list.size();
                default:
                    return NOT_FOUND;
                }
            }

            @Override
            public Object get(int index, Scriptable start) {
                if (index < 0 || index >= list.size())
                    return NOT_FOUND;
                return javaToJS(list.get(index), scope);
            }

            @Override
            public boolean has(String name, Scriptable start) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean has(int index, Scriptable start) {
                return 0 <= index && index < list.size();
            }

            @Override
            public void put(String name, Scriptable start, Object value) {
                throw new UnsupportedOperationException();
            }

            @SuppressWarnings("unchecked")
            @Override
            public void put(int index, Scriptable start, Object value) {
                int size = list.size();
                if (0 <= index && index < size)
                    ((List<Object>) list).set(index, fromNative(value, scope));
                else if (index == size + 1)
                    ((List<Object>) list).add(fromNative(value, scope));
                else
                    throw new RuntimeException("JSList does not allow put at random index");
            }

            @Override
            public void delete(String name) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void delete(int index) {
                list.remove(index);
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
                Object[] ids = new Object[list.size()];
                for (int i = 0; i < list.size(); ++i)
                    ids[i] = i;
                return ids;
            }

            @Override
            public Object getDefaultValue(Class<?> hint) {
                return "[object JSList]";
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
    
    static Scriptable toScriptable(final Map<?, ?> map, final Scriptable scope) {
        return new Scriptable() {

            private Scriptable prototype, parent;

            @Override
            public String getClassName() {
                return "JSMap";
            }

            @Override
            public Object get(String name, Scriptable start) {
                if (map.containsKey(name)) {
                    return javaToJS(map.get(name), scope);
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
                ((Map<String, Object>) map).put(name, fromNative(value, scope));
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
                return "[object JSMap]";
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

    private static Scriptable toScriptable(final JsonElement json, final Scriptable scope) {
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
                        return javaToJS(json.asObject().getField(name), scope);
                    } else {
                        return NOT_FOUND;
                    }
                } else {
                    switch (name) {
                    case "length":
                        return json.asArray().size();
                    default:
                        return NOT_FOUND;
                    }
                }
            }

            @Override
            public Object get(int index, Scriptable start) {
                if (json.isArray()) {
                    if (index >= 0 && json.asArray().size() > index) {
                        return javaToJS(json.asArray().get(index), scope);
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
                if (!json.isObject())
                    throw new RuntimeException("Not a JsonObject.");
                json.asObject().putValue(name, fromNative(value, scope));
            }

            @Override
            public void put(int index, Scriptable start, Object value) {
                if (!json.isArray())
                    throw new RuntimeException("Not a JsonArray.");
                JsonArray arr = json.asArray();
                if (index != arr.size() + 1)
                    throw new RuntimeException("JsonArray does not allow put at random index");
                arr.add(fromNative(value, scope));
            }

            @Override
            public void delete(String name) {
                if (!json.isObject())
                    throw new RuntimeException("Not a JsonObject.");
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
                if (json.isArray()) {
                    int size = json.asArray().size();
                    Object[] ids = new Object[size];
                    for (int i = 0; i < size; ++i) ids[i] = i;
                    return ids;
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

    private static Scriptable toScriptable(final ChangeAwareJsonElement json, final Scriptable scope) {
        return new Scriptable() {

            private Scriptable prototype, parent;

            @Override
            public String getClassName() {
                return "JSChangeAwareJsonElement";
            }

            @Override
            public Object get(String name, Scriptable start) {
                if (json.isObject()) {
                    if (json.asObject().containsField(name)) {
                        return javaToJS(json.asObject().getField(name), scope);
                    } else {
                        return NOT_FOUND;
                    }
                } else {
                    switch (name) {
                    case "length":
                        return json.asArray().size();
                    default:
                        return NOT_FOUND;
                    }
                }
            }

            @Override
            public Object get(int index, Scriptable start) {
                if (json.isArray()) {
                    if (index >= 0 && json.asArray().size() > index) {
                        return javaToJS(json.asArray().get(index), scope);
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
                if (!json.isObject())
                    throw new RuntimeException("Not a ChangeAwareJsonObject.");
                json.asObject().putValue(name, fromNative(value, scope));
            }

            @Override
            public void put(int index, Scriptable start, Object value) {
                if (!json.isArray())
                    throw new RuntimeException("Not a ChangeAwareJsonArray.");
                ChangeAwareJsonArray arr = json.asArray();
                if (index != arr.size() + 1)
                    throw new RuntimeException("ChangeAwareJsonArray does not allow put at random index");
                arr.add(fromNative(value, scope));
            }

            @Override
            public void delete(String name) {
                if (!json.isObject())
                    throw new RuntimeException("Not a ChangeAwareJsonObject.");
                json.asObject().removeField(name);
            }

            @Override
            public void delete(int index) {
                throw new RuntimeException("ChangeAwareJsonArray does not allow delete at random index");
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
                if (json.isArray()) {
                    int size = json.asArray().size();
                    Object[] ids = new Object[size];
                    for (int i = 0; i < size; ++i) ids[i] = i;
                    return ids;
                }
                return EMPTY_OBJECT_ARRAY;
            }

            @Override
            public Object getDefaultValue(Class<?> hint) {
                return "[object JSChangeAwareJsonElement]";
            }

            @Override
            public boolean hasInstance(Scriptable instance) {
                return instance != null && instance instanceof JsonElement;
            }
        };
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object fromNative(Object value, final Scriptable scope) {
        if (value == null) return null;

        boolean isNativeArray = value instanceof NativeArray,
                isNativeObject = value instanceof NativeObject;
        if (isNativeArray || isNativeObject) {
            // Convert JavaScript json to vertx JsonArray or JsonObject
            Object json = NativeJSON.stringify(Context.getCurrentContext(), scope, value, null, null);
            if (json instanceof String) {
                if (isNativeArray) {
                    value = new JsonArray((List) Json.decodeValue((String) json, List.class));
                } else {
                    value = new JsonObject((Map) Json.decodeValue((String) json, Map.class));
                }
            }
        }

        if (value instanceof Double) {
            // Because JavaScripe Number will be default to Double, it is better
            // to convert it to an integer long value if it could be represented to.
            double doubleVal = (Double) value;
            long longVal = Math.round(doubleVal);
            if (Math.abs(doubleVal - longVal) < Epsilon)
                value = longVal;
        }

        if (value instanceof CharSequence) {
        	// Need to convert CharSequence to String
        	value = value.toString();
        }

        return value;
    }
    private static final double Epsilon = 0.0000001;

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
