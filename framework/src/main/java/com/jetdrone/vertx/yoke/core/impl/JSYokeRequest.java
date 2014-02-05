package com.jetdrone.vertx.yoke.core.impl;

import com.jetdrone.vertx.yoke.core.Context;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.store.SessionStore;
import org.mozilla.javascript.Scriptable;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.*;

final class JSYokeRequest  extends YokeRequest implements Scriptable {

    private static final Map<String, JSProperty> JS_PROPERTIES = new HashMap<String, JSProperty>() {{
        // members
        put("response", null);
        put("params", new JSProperty(YokeRequest.class, "params", true));
        put("headers", new JSProperty(YokeRequest.class, "headers", true));
        put("bodyLengthLimit", new JSProperty(YokeRequest.class, "bodyLengthLimit", true));
        put("contentLength", new JSProperty(YokeRequest.class, "contentLength", true));
        put("version", new JSProperty(YokeRequest.class, "version", true));
        put("method", new JSProperty(YokeRequest.class, "method", true));
        put("uri", new JSProperty(YokeRequest.class, "uri", true));
        put("path", new JSProperty(YokeRequest.class, "path", true));
        put("query", new JSProperty(YokeRequest.class, "query", true));
        put("normalizedPath", new JSProperty(YokeRequest.class, "normalizedPath", true));
        put("remoteAddress", new JSProperty(YokeRequest.class, "remoteAddress", true));
        put("peerCertificateChain", new JSProperty(YokeRequest.class, "peerCertificateChain", true));
        put("absoluteURI", new JSProperty(YokeRequest.class, "absoluteURI", true));
        put("netSocket", new JSProperty(YokeRequest.class, "netSocket", true));
        put("formAttributes", new JSProperty(YokeRequest.class, "formAttributes", true));
        put("localAddress", new JSProperty(YokeRequest.class, "localAddress", true));
        put("ip", new JSProperty(YokeRequest.class, "ip", true));
        put("cookies", new JSProperty(YokeRequest.class, "cookies", true));
        put("body", new JSProperty(YokeRequest.class, "body", true));
        put("files", new JSProperty(YokeRequest.class, "files", true));
        // methods
        put("hasBody", new JSProperty(YokeRequest.class, "hasBody"));
        put("destroySession", new JSProperty(YokeRequest.class, "destroySession"));
        put("loadSession", new JSProperty(YokeRequest.class, "hasBody"));
        put("createSession", new JSProperty(YokeRequest.class, "hasBody"));
        put("isSecure", new JSProperty(YokeRequest.class, "hasBody"));
        put("accepts", new JSProperty(YokeRequest.class, "hasBody"));
        put("is", new JSProperty(YokeRequest.class, "hasBody"));
        put("vertxHttpServerRequest", new JSProperty(YokeRequest.class, "hasBody"));
        put("bodyHandler", new JSProperty(YokeRequest.class, "hasBody"));
        put("expectMultiPart", new JSProperty(YokeRequest.class, "hasBody"));
        put("uploadHandler", new JSProperty(YokeRequest.class, "hasBody"));
        put("dataHandler", new JSProperty(YokeRequest.class, "hasBody"));
        put("pause", new JSProperty(YokeRequest.class, "hasBody"));
        put("resume", new JSProperty(YokeRequest.class, "hasBody"));
        put("endHandler", new JSProperty(YokeRequest.class, "hasBody"));
        put("exceptionHandler", new JSProperty(YokeRequest.class, "hasBody"));
    }};

    private final Context context;

    private Scriptable jsFiles;
    private Scriptable jsParams;
    private Scriptable jsFormAttributes;
    private Scriptable jsHeaders;
    private Scriptable jsCookies;

    private Scriptable prototype, parent;

    public JSYokeRequest(HttpServerRequest request, JSYokeResponse response, boolean secure, Context context, SessionStore store) {
        super(request, response, secure, context, store);
        this.context = context;
    }

    @Override
    public String getClassName() {
        return "JSYokeRequest";
    }

    @Override
    public Object get(String name, Scriptable start) {
        // first context
        if (context.containsKey(name)) {
            return context.get(name);
        }
        // cacheable scriptable objects
        switch (name) {
            case "response":
                return response();
            case "params":
                if (jsParams == null) {
                    jsParams = (Scriptable) JS_PROPERTIES.get(name).getValue(this);
                }
                return jsParams;
            case "headers":
                if (jsHeaders == null) {
                    jsHeaders = (Scriptable) JS_PROPERTIES.get(name).getValue(this);
                }
                return jsHeaders;
            case "formAttributes":
                if (jsFormAttributes == null) {
                    jsFormAttributes = (Scriptable) JS_PROPERTIES.get(name).getValue(this);
                }
                return jsFormAttributes;
            case "cookies":
                if (jsCookies == null) {
                    jsCookies = (Scriptable) JS_PROPERTIES.get(name).getValue(this);
                }
                return jsCookies;
            case "files":
                if (jsFiles == null) {
                    jsFiles = (Scriptable) JS_PROPERTIES.get(name).getValue(this);
                }
                return jsFiles;
        }
        // then members
        if (JS_PROPERTIES.containsKey(name)) {
            return JS_PROPERTIES.get(name).getValue(this);
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
        return "[object JSYokeRequest]";
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