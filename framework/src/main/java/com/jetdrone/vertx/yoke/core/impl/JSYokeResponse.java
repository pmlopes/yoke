package com.jetdrone.vertx.yoke.core.impl;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.core.Context;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;
import org.mozilla.javascript.Scriptable;
import org.vertx.java.core.http.HttpServerResponse;

import java.util.HashMap;
import java.util.Map;

final class JSYokeResponse  extends YokeResponse implements Scriptable {

    private static final Map<String, JSProperty> JS_PROPERTIES = new HashMap<>();

    static {
        JS_PROPERTIES.put("putTrailer", new JSProperty(YokeResponse.class, "putTrailer"));
        JS_PROPERTIES.put("setStatusCode", new JSProperty(YokeResponse.class, "setStatusCode"));
        JS_PROPERTIES.put("getStatusCode", new JSProperty(YokeResponse.class, "getStatusCode"));

        JS_PROPERTIES.put("setStatusMessage", new JSProperty(YokeResponse.class, "setStatusMessage"));
        JS_PROPERTIES.put("getStatusMessage", new JSProperty(YokeResponse.class, "getStatusMessage"));

        JS_PROPERTIES.put("jsonp", new JSProperty(YokeResponse.class, "jsonp"));

        JS_PROPERTIES.put("addCookie", new JSProperty(YokeResponse.class, "addCookie"));

        JS_PROPERTIES.put("isChunked", new JSProperty(YokeResponse.class, "isChunked"));
        JS_PROPERTIES.put("setChunked", new JSProperty(YokeResponse.class, "setChunked"));

        JS_PROPERTIES.put("closeHandler", new JSProperty(YokeResponse.class, "closeHandler"));
        JS_PROPERTIES.put("sendFile", new JSProperty(YokeResponse.class, "sendFile"));
        JS_PROPERTIES.put("trailers", new JSProperty(YokeResponse.class, "trailers"));

        JS_PROPERTIES.put("redirect", new JSProperty(YokeResponse.class, "redirect"));
        JS_PROPERTIES.put("getHeader", new JSProperty(YokeResponse.class, "getHeader"));
        JS_PROPERTIES.put("headersHandler", new JSProperty(YokeResponse.class, "headersHandler"));
        JS_PROPERTIES.put("exceptionHandler", new JSProperty(YokeResponse.class, "exceptionHandler"));

        JS_PROPERTIES.put("drainHandler", new JSProperty(YokeResponse.class, "drainHandler"));
        JS_PROPERTIES.put("setWriteQueueMaxSize", new JSProperty(YokeResponse.class, "setWriteQueueMaxSize"));
        JS_PROPERTIES.put("writeQueueFull", new JSProperty(YokeResponse.class, "writeQueueFull"));
        JS_PROPERTIES.put("endHandler", new JSProperty(YokeResponse.class, "endHandler"));
        JS_PROPERTIES.put("putHeader", new JSProperty(YokeResponse.class, "putHeader"));

        JS_PROPERTIES.put("headers", new JSProperty(YokeResponse.class, "headers"));
        JS_PROPERTIES.put("setContentType", new JSProperty(YokeResponse.class, "setContentType"));
        JS_PROPERTIES.put("render", new JSProperty(YokeResponse.class, "render"));
        JS_PROPERTIES.put("write", new JSProperty(YokeResponse.class, "write"));
        JS_PROPERTIES.put("close", new JSProperty(YokeResponse.class, "close"));
        JS_PROPERTIES.put("end", new JSProperty(YokeResponse.class, "end"));
    }

    public JSYokeResponse(HttpServerResponse response, Context context, Map<String, Engine> engines) {
        super(response, context, engines);
        this.context = context;
    }

    private final Context context;

    private Scriptable prototype, parent;

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
        // cacheable scriptable objects
        switch (name) {
            default:
                // then members
                if (JS_PROPERTIES.containsKey(name)) {
                    return JS_PROPERTIES.get(name).getValue(this);
                }
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
        if (JS_PROPERTIES.containsKey(name)) {
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
