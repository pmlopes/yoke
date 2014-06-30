package com.jetdrone.vertx.yoke.core.impl;

import java.util.List;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;

class JSUtil {

    private JSUtil() {}

    static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    static Object javaToJS(Object value, Scriptable scope) {
    	if (value instanceof MultiMap)
    		return toScriptable((MultiMap) value, scope);
    	if (value instanceof Set)
    		return toScriptable((Set<?>) value, scope);
    	if (value instanceof JsonElement)
    		return toScriptable((JsonElement) value, scope);
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
    	Object[] elements = set.toArray();
    	final Scriptable inner = Context.toObject(elements, scope);
    	
        return new Scriptable() {

            @Override
            public String getClassName() {
                return "JSSet";
            }

            @Override
            public Object get(String name, Scriptable start) {
                return inner.get(name, start);
            }

            @Override
            public Object get(int index, Scriptable start) {
            	return inner.get(index, start);
            }

            @Override
            public boolean has(String name, Scriptable start) {
                return inner.has(name, start);
            }

            @Override
            public boolean has(int index, Scriptable start) {
            	return inner.has(index, start);
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
                return inner.getPrototype();
            }

            @Override
            public void setPrototype(Scriptable prototype) {
                inner.setPrototype(prototype);
            }

            @Override
            public Scriptable getParentScope() {
                return inner.getParentScope();
            }

            @Override
            public void setParentScope(Scriptable parent) {
                inner.setParentScope(parent);
            }

            @Override
            public Object[] getIds() {
                return inner.getIds();
            }

            @Override
            public Object getDefaultValue(Class<?> hint) {
                return "[object JSSet]";
            }

            @Override
            public boolean hasInstance(Scriptable instance) {
                return inner.hasInstance(instance);
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
                    return NOT_FOUND;
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
                json.asObject().putValue(name, value);
            }

            @Override
            public void put(int index, Scriptable start, Object value) {
            	if (!json.isArray())
            		throw new RuntimeException("Not a JsonArray.");
            	JsonArray arr = json.asArray();
            	if (index != arr.size() + 1)
            		throw new RuntimeException("JsonArray does not allow put at random index");
            	arr.add(value);
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
                	for (int i = 0; i < size; ++i) ids[i] = Integer.valueOf(i);
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
