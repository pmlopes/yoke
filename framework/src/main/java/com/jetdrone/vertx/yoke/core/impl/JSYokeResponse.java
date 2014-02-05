package com.jetdrone.vertx.yoke.core.impl;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.core.Context;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;
import org.mozilla.javascript.Scriptable;
import org.vertx.java.core.http.HttpServerResponse;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class JSYokeResponse  extends YokeResponse implements Scriptable {

    private static final Set<String> jsProperties = new HashSet<>(Arrays.asList(
            // members

            // methods
            "end"
    ));

    private static final Method end = getMethod("end", String.class);

    private static Method getMethod(String name, Class<?>... parameterTypes) {
        try {
            return YokeResponse.class.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    public JSYokeResponse(HttpServerResponse response, Context context, Map<String, Engine> engines) {
        super(response, context, engines);
        this.context = context;
    }

    private final Context context;

    private Scriptable prototype, parent;

    private Object getProperty(String name) {
        switch (name) {
            case "end":
                return JSUtil.wrapFunction(end);
            default:
                return NOT_FOUND;
        }
    }

    @Override
    public String getClassName() {
        return "JSYokeResponse";
    }

    @Override
    public Object get(String name, Scriptable start) {
        // first context
        if (context.containsKey(name)) {
            return context.get(name);
        }
        // then members
        if (jsProperties.contains(name)) {
            return getProperty(name);
        }
        // fail to find
        return NOT_FOUND;
    }

    @Override
    public Object get(int index, Scriptable start) {
        return get(Integer.toString(index), start);
    }

    @Override
    public boolean has(String name, Scriptable start) {
        // first context
        if (context.containsKey(name)) {
            return true;
        }
        // then functions
        if (jsProperties.contains(name)) {
            return true;
        }
        // fail to find
        return false;
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return has(Integer.toString(index), start);
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        context.put(name, value);
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        put(Integer.toString(index), start, value);
    }

    @Override
    public void delete(String name) {
        context.remove(name);
    }

    @Override
    public void delete(int index) {
        delete(Integer.toString(index));
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
        return context.keySet().toArray();
    }

    @Override
    public Object getDefaultValue(Class<?> hint) {
        return "[object JSYokeResponse]";
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
}
