package com.jetdrone.vertx.yoke.core.impl;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;
import org.vertx.java.core.MultiMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JSProperty {

    private enum WrapType {
        MultiMap,
        Map,
        List,
        None
    }

    private static Scriptable wrapMultiMap(final MultiMap multiMap) {
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

    private static Scriptable wrapList(final List list) {
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

    private static Scriptable wrapMap(final Map map) {
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

    static Function wrapFunction(final Method[] methods) {
        return new Function() {
            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                try {
                    for (Method m : methods) {
                        final Class<?>[] parameterTypes = m.getParameterTypes();
                        if (args == null && parameterTypes == null) {
                            // js called without params
                            // java method has no params
                            // match
                            if (Modifier.isStatic(m.getModifiers())) {
                                return m.invoke(null);
                            } else {
                                return m.invoke(thisObj);
                            }
                        }

                        if (args != null && parameterTypes != null) {
                            // js called with params
                            // java method has params

                            if (args.length == parameterTypes.length) {
                                // TODO: validate that the types are the same, and do any casts if needed
                                if (Modifier.isStatic(m.getModifiers())) {
                                    return m.invoke(null, args);
                                } else {
                                    return m.invoke(thisObj, args);
                                }
                            }
                        }
                    }

                    throw new IllegalAccessException("No method compatible found!");

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

    private final String name;
    private final Method[] methods;
    private final boolean asMember;
    private final WrapType wrapType;

    public JSProperty(Class clazz, String name) {
        this(clazz, name, name, false);
    }

    public JSProperty(Class clazz, String name, boolean asMember) {
        this(clazz, name, name, asMember);
    }

    public JSProperty(Class clazz, String name, String jsName) {
        this(clazz, name, jsName, false);
    }

    public JSProperty(Class clazz, String name, String jsName, boolean asMember) {

        this.name = jsName;
        this.asMember = asMember;

        List<Method> methodList = new ArrayList<>();

        for (Method m : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers())) {
                if (m.getName().equals(name)) {
                    methodList.add(m);
                }
            }
        }

        // validation
        if (methodList.size() == 0) {
            throw new RuntimeException("Java method not found: " + name);
        }

        if (asMember) {
            if (methodList.size() > 1) {
                // keep the last
                methods = new Method[] {methodList.get(methodList.size() - 1)};
            } else {
                methods = methodList.toArray(new Method[methodList.size()]);
            }

            Class[] paramTypes = methods[0].getParameterTypes();
            if (paramTypes != null && paramTypes.length > 0) {
                throw new RuntimeException("Java method requires parameters while JS expects a member: " + name);
            }
        } else {
            methods = methodList.toArray(new Method[methodList.size()]);
        }

        WrapType wrapType = WrapType.None;
        Class<?> retType = methods[0].getReturnType();

        if (retType.isAssignableFrom(MultiMap.class)) {
            wrapType = WrapType.MultiMap;
        }
        if (retType.isAssignableFrom(Map.class)) {
            wrapType = WrapType.Map;
        }
        if (retType.isAssignableFrom(List.class)) {
            wrapType = WrapType.List;
        }

        this.wrapType = wrapType;
    }

    public boolean isScriptable() {
        if (asMember) {
            switch (wrapType) {
                case MultiMap:
                case Map:
                case List:
                    return true;
                case None:
                default:
                    return false;
            }
        } else {
            return true;
        }
    }

    public Object getValue(Object thisObj) {
        try {
            if (asMember) {
                // on the java side we have a method without arguments, however we will expose it as a member property
                switch (wrapType) {
                    case MultiMap:
                        return wrapMultiMap((MultiMap) methods[0].invoke(thisObj));
                    case Map:
                        return wrapMap((Map) methods[0].invoke(thisObj));
                    case List:
                        return wrapList((List) methods[0].invoke(thisObj));
                    case None:
                        return methods[0].invoke(thisObj);
                    default:
                        throw new RuntimeException("Should not happen!");
                }
            } else {
                // expose as method also on JavaScript
                return wrapFunction(methods);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new WrappedException(e);
        }
    }
}
