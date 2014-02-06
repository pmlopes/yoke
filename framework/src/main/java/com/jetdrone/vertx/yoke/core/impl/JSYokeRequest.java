package com.jetdrone.vertx.yoke.core.impl;

import com.jetdrone.vertx.yoke.core.Context;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.store.SessionStore;
import org.mozilla.javascript.Scriptable;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.*;

final class JSYokeRequest  extends YokeRequest implements Scriptable {

    private static final Map<String, JSProperty> JS_PROPERTIES = new HashMap<>();

    static {
        // members
        JS_PROPERTIES.put("response", null);
        JS_PROPERTIES.put("params", new JSProperty(YokeRequest.class, "params", true));
        JS_PROPERTIES.put("headers", new JSProperty(YokeRequest.class, "headers", true));
        JS_PROPERTIES.put("bodyLengthLimit", new JSProperty(YokeRequest.class, "bodyLengthLimit", true));
        JS_PROPERTIES.put("contentLength", new JSProperty(YokeRequest.class, "contentLength", true));
        JS_PROPERTIES.put("version", new JSProperty(YokeRequest.class, "version", true));
        JS_PROPERTIES.put("method", new JSProperty(YokeRequest.class, "method", true));
        JS_PROPERTIES.put("uri", new JSProperty(YokeRequest.class, "uri", true));
        JS_PROPERTIES.put("path", new JSProperty(YokeRequest.class, "path", true));
        JS_PROPERTIES.put("query", new JSProperty(YokeRequest.class, "query", true));
        JS_PROPERTIES.put("normalizedPath", new JSProperty(YokeRequest.class, "normalizedPath", true));
        JS_PROPERTIES.put("remoteAddress", new JSProperty(YokeRequest.class, "remoteAddress", true));
        JS_PROPERTIES.put("peerCertificateChain", new JSProperty(YokeRequest.class, "peerCertificateChain", true));
        JS_PROPERTIES.put("absoluteURI", new JSProperty(YokeRequest.class, "absoluteURI", true));
        JS_PROPERTIES.put("netSocket", new JSProperty(YokeRequest.class, "netSocket", true));
        JS_PROPERTIES.put("formAttributes", new JSProperty(YokeRequest.class, "formAttributes", true));
        JS_PROPERTIES.put("localAddress", new JSProperty(YokeRequest.class, "localAddress", true));
        JS_PROPERTIES.put("ip", new JSProperty(YokeRequest.class, "ip", true));
        JS_PROPERTIES.put("cookies", new JSProperty(YokeRequest.class, "cookies", true));
        JS_PROPERTIES.put("body", new JSProperty(YokeRequest.class, "body", true));
        JS_PROPERTIES.put("files", new JSProperty(YokeRequest.class, "files", true));
        // methods
        JS_PROPERTIES.put("hasBody", new JSProperty(YokeRequest.class, "hasBody"));
        JS_PROPERTIES.put("destroySession", new JSProperty(YokeRequest.class, "destroySession"));
        JS_PROPERTIES.put("loadSession", new JSProperty(YokeRequest.class, "loadSession"));
        JS_PROPERTIES.put("createSession", new JSProperty(YokeRequest.class, "createSession"));
        JS_PROPERTIES.put("isSecure", new JSProperty(YokeRequest.class, "isSecure"));
        JS_PROPERTIES.put("accepts", new JSProperty(YokeRequest.class, "accepts"));
        JS_PROPERTIES.put("is", new JSProperty(YokeRequest.class, "is"));
        JS_PROPERTIES.put("httpServerRequest", new JSProperty(YokeRequest.class, "vertxHttpServerRequest"));
        JS_PROPERTIES.put("bodyHandler", new JSProperty(YokeRequest.class, "bodyHandler"));
        JS_PROPERTIES.put("expectMultiPart", new JSProperty(YokeRequest.class, "expectMultiPart"));
        JS_PROPERTIES.put("uploadHandler", new JSProperty(YokeRequest.class, "uploadHandler"));
        JS_PROPERTIES.put("dataHandler", new JSProperty(YokeRequest.class, "dataHandler"));
        JS_PROPERTIES.put("pause", new JSProperty(YokeRequest.class, "pause"));
        JS_PROPERTIES.put("resume", new JSProperty(YokeRequest.class, "resume"));
        JS_PROPERTIES.put("endHandler", new JSProperty(YokeRequest.class, "endHandler"));
        JS_PROPERTIES.put("exceptionHandler", new JSProperty(YokeRequest.class, "exceptionHandler"));
    }

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
                // special case
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
        return instance != null && instance instanceof JSYokeRequest;
    }
}