package com.jetdrone.vertx.yoke.core.impl;

import org.mozilla.javascript.Scriptable;
import org.vertx.java.core.MultiMap;

class JSUtil {

    private JSUtil() {}

    static Scriptable toScriptable(final MultiMap multiMap) {
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
}
