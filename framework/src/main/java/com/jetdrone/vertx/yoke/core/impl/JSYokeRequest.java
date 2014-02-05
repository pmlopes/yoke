package com.jetdrone.vertx.yoke.core.impl;

import com.jetdrone.vertx.yoke.core.Context;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.store.SessionStore;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class JSYokeRequest  extends YokeRequest implements Scriptable {

    private static final Set<String> jsProperties = new HashSet<>(Arrays.asList(
            // members
            "response",
            "params",
            "headers",
            "bodyLengthLimit",
            "contentLength",
            "version",
            "method",
            "uri",
            "path",
            "query",
            "normalizedPath",
            "remoteAddress",
            "peerCertificateChain",
            "absoluteURI",
            "netSocket",
            "formAttributes",
            "localAddress",
            "ip",
            "cookies",
            "body",
            "files",
            // methods
            "hasBody",
            "destroySession",
            "loadSession",
            "createSession",
            "isSecure",
            "accepts",
            "is",
            "vertxHttpServerRequest",
            "bodyHandler",
            "expectMultiPart",
            "uploadHandler",
            "dataHandler",
            "pause",
            "resume",
            "endHandler",
            "exceptionHandler"
    ));

    private static final Method hasBody = getMethod("hasBody");
    private static final Method pause = getMethod("pause");
    private static final Method resume = getMethod("resume");
    private static final Method isSecure = getMethod("isSecure");
    private static final Method destroySession = getMethod("destroySession");
    private static final Method loadSession = getMethod("loadSession", String.class, Handler.class);
    private static final Method createSession = getMethod("createSession");
    private static final Method accepts = getMethod("accepts", String[].class);
    private static final Method is = getMethod("is", String.class);
    private static final Method vertxHttpServerRequest = getMethod("vertxHttpServerRequest");
    private static final Method bodyHandler = getMethod("bodyHandler", Handler.class);
    private static final Method expectMultiPart = getMethod("expectMultiPart", boolean.class);
    private static final Method uploadHandler = getMethod("uploadHandler", Handler.class);
    private static final Method dataHandler = getMethod("dataHandler", Handler.class);
    private static final Method endHandler = getMethod("endHandler", Handler.class);
    private static final Method exceptionHandler = getMethod("exceptionHandler", Handler.class);

    private final Context context;

    private Scriptable prototype, parent;

    private Scriptable files;
    private Scriptable params;
    private Scriptable formAttributes;
    private Scriptable headers;

    private final Scriptable response;

    public JSYokeRequest(HttpServerRequest request, JSYokeResponse response, boolean secure, Context context, SessionStore store) {
        super(request, response, secure, context, store);
        this.context = context;
        this.response = response;

    }

    private static Method getMethod(String name, Class<?>... parameterTypes) {
        try {
            return YokeRequest.class.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getProperty(String name) {
        switch (name) {
            // members
            case "response":
                return response;
            case "params":
                if (params == null) {
                    params = JSUtil.wrapMultiMap(params());
                }
                return params;
            case "headers":
                if (headers == null) {
                    headers = JSUtil.wrapMultiMap(headers());
                }
                return headers;
            case "bodyLengthLimit":
                return bodyLengthLimit();
            case "contentLength":
                return contentLength();
            case "version":
                return version();
            case "method":
                return method();
            case "uri":
                return uri();
            case "path":
                return path();
            case "query":
                return query();
            case "normalizedPath":
                return normalizedPath();
            case "remoteAddress":
                return remoteAddress();
            case "peerCertificateChain":
                try {
                    return peerCertificateChain();
                } catch (SSLPeerUnverifiedException e) {
                    throw new WrappedException(e);
                }
            case "absoluteURI":
                return absoluteURI();
            case "netSocket":
                return netSocket();
            case "formAttributes":
                if (formAttributes == null) {
                    formAttributes = JSUtil.wrapMultiMap(formAttributes());
                }
                return formAttributes;
            case "localAddress":
                return localAddress();
            case "ip":
                return ip();
            case "cookies":
                throw new RuntimeException("Not Implemented");
            case "body":
                throw new RuntimeException("Not Implemented");
            case "files":
                throw new RuntimeException("Not Implemented");
//                if (files == null) {
//                    files = JSUtil.wrapMap(files());
//                }
//                return files;

            // methods
            case "hasBody":
                return JSUtil.wrapFunction(hasBody);
            case "destroySession":
                return JSUtil.wrapFunction(destroySession);
            case "loadSession":
                throw new RuntimeException("Not Implemented");
            case "createSession":
                throw new RuntimeException("Not Implemented");
            case "isSecure":
                return JSUtil.wrapFunction(isSecure);
            case "accepts":
                throw new RuntimeException("Not Implemented");
            case "is":
                throw new RuntimeException("Not Implemented");
            case "vertxHttpServerRequest":
                throw new RuntimeException("Not Implemented");
            case "bodyHandler":
                throw new RuntimeException("Not Implemented");
            case "expectMultiPart":
                throw new RuntimeException("Not Implemented");
            case "uploadHandler":
                throw new RuntimeException("Not Implemented");
            case "dataHandler":
                throw new RuntimeException("Not Implemented");
            case "pause":
                return JSUtil.wrapFunction(pause);
            case "resume":
                return JSUtil.wrapFunction(resume);
            case "endHandler":
                throw new RuntimeException("Not Implemented");
            case "exceptionHandler":
                throw new RuntimeException("Not Implemented");
            default:
                return NOT_FOUND;
        }
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