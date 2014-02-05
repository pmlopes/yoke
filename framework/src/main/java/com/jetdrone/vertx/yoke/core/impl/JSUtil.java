package com.jetdrone.vertx.yoke.core.impl;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;
import org.vertx.java.core.MultiMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

final class JSUtil {

    private JSUtil() {}

    static Function wrapFunction(final Method m) {
        return new Function() {
            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                try {
                    return m.invoke(thisObj, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new WrappedException(e);
                }
            }

            @Override
            public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
                return null;
            }

            @Override
            public String getClassName() {
                return "JSYokeFunction";
            }

            @Override
            public Object get(String name, Scriptable start) {
                return NOT_FOUND;
            }

            @Override
            public Object get(int index, Scriptable start) {
                return NOT_FOUND;
            }

            @Override
            public boolean has(String name, Scriptable start) {
                return false;
            }

            @Override
            public boolean has(int index, Scriptable start) {
                return false;
            }

            @Override
            public void put(String name, Scriptable start, Object value) {

            }

            @Override
            public void put(int index, Scriptable start, Object value) {

            }

            @Override
            public void delete(String name) {

            }

            @Override
            public void delete(int index) {

            }

            @Override
            public Scriptable getPrototype() {
                return null;
            }

            @Override
            public void setPrototype(Scriptable prototype) {

            }

            @Override
            public Scriptable getParentScope() {
                return null;
            }

            @Override
            public void setParentScope(Scriptable parent) {

            }

            @Override
            public Object[] getIds() {
                return new Object[0];
            }

            @Override
            public Object getDefaultValue(Class<?> hint) {
                return "[object JSYokeFunction]";
            }

            @Override
            public boolean hasInstance(Scriptable instance) {
                return false;
            }
        };
    }

    static Scriptable wrapMultiMap(final MultiMap multiMap) {
        return new Scriptable() {

            private Scriptable prototype, parent;

            @Override
            public String getClassName() {
                return "JSMultiMap";
            }

            @Override
            public Object get(String name, Scriptable start) {
                return multiMap.get(name);
            }

            @Override
            public Object get(int index, Scriptable start) {
                return multiMap.get(Integer.toString(index));
            }

            @Override
            public boolean has(String name, Scriptable start) {
                return multiMap.contains(name);
            }

            @Override
            public boolean has(int index, Scriptable start) {
                return multiMap.contains(Integer.toString(index));
            }

            @Override
            public void put(String name, Scriptable start, Object value) {
                // TODO: cast to the right type
                //multiMap.add(name, value);
            }

            @Override
            public void put(int index, Scriptable start, Object value) {
                // TODO: cast to the right type
                //multiMap.add(Integer.toString(index), value);
            }

            @Override
            public void delete(String name) {
                multiMap.remove(name);
            }

            @Override
            public void delete(int index) {
                multiMap.remove(Integer.toString(index));
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

    static Scriptable wrapList(final List<Object> list) {
        return new Scriptable() {
            @Override
            public String getClassName() {
                return "JSYokeCollection";
            }

            @Override
            public Object get(String name, Scriptable start) {
                return NOT_FOUND;
            }

            @Override
            public Object get(int index, Scriptable start) {
                return list.get(index);
            }

            @Override
            public boolean has(String name, Scriptable start) {
                return false;
            }

            @Override
            public boolean has(int index, Scriptable start) {
                return index >= 0 && index < list.size();
            }

            @Override
            public void put(String name, Scriptable start, Object value) {

            }

            @Override
            public void put(int index, Scriptable start, Object value) {

            }

            @Override
            public void delete(String name) {

            }

            @Override
            public void delete(int index) {

            }

            @Override
            public Scriptable getPrototype() {
                return null;
            }

            @Override
            public void setPrototype(Scriptable prototype) {

            }

            @Override
            public Scriptable getParentScope() {
                return null;
            }

            @Override
            public void setParentScope(Scriptable parent) {

            }

            @Override
            public Object[] getIds() {
                return new Object[0];
            }

            @Override
            public Object getDefaultValue(Class<?> hint) {
                return "[object JSYokeCollection]";
            }

            @Override
            public boolean hasInstance(Scriptable instance) {
                return false;
            }
        };
    }

    static Scriptable wrapMap(final Map<String, Object> map) {
        return new Scriptable() {
            @Override
            public String getClassName() {
                return "JSYokeCollection";
            }

            @Override
            public Object get(String name, Scriptable start) {
                return map.get(name);
            }

            @Override
            public Object get(int index, Scriptable start) {
                return NOT_FOUND;
            }

            @Override
            public boolean has(String name, Scriptable start) {
                return map.containsKey(name);
            }

            @Override
            public boolean has(int index, Scriptable start) {
                return false;
            }

            @Override
            public void put(String name, Scriptable start, Object value) {
                map.put(name, value);
            }

            @Override
            public void put(int index, Scriptable start, Object value) {
                map.put(Integer.toString(index), value);
            }

            @Override
            public void delete(String name) {
                map.remove(name);
            }

            @Override
            public void delete(int index) {
                map.remove(Integer.toString(index));
            }

            @Override
            public Scriptable getPrototype() {
                return null;
            }

            @Override
            public void setPrototype(Scriptable prototype) {

            }

            @Override
            public Scriptable getParentScope() {
                return null;
            }

            @Override
            public void setParentScope(Scriptable parent) {

            }

            @Override
            public Object[] getIds() {
                return new Object[0];
            }

            @Override
            public Object getDefaultValue(Class<?> hint) {
                return "[object JSYokeCollection]";
            }

            @Override
            public boolean hasInstance(Scriptable instance) {
                return false;
            }
        };
    }

}
