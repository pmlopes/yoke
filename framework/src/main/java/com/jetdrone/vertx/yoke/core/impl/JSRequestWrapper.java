package com.jetdrone.vertx.yoke.core.impl;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.core.RequestWrapper;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;
import com.jetdrone.vertx.yoke.store.SessionStore;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JSRequestWrapper implements RequestWrapper {

    private static Function wrapFunction(Method m) {
        System.err.println(m.getName());
        return null;
    }

    private static class JSYokeRequest extends YokeRequest implements Scriptable {

        private Scriptable prototype, parent;

        private final Map<String, Function> fns = new HashMap<>();
        private final Map<String, Object> context;

        public JSYokeRequest(HttpServerRequest request, YokeResponse response, boolean secure, Map<String, Object> context, SessionStore store) {
            super(request, response, secure, context, store);
            this.context = context;

//    getHeader
//    getHeader
//    response
//    response
//    bodyLengthLimit
//    getAllHeaders
//    getAllCookies
//    originalMethod
//    setBodyLengthLimit
//    hasBody
//    setFiles
//    setCookies
//    destroySession
//    loadSession
//    createSession
//    isSecure
//    splitMime
//    accepts
//    ip
//    getParameter
//    getParameter
//    getParameterList
//    getFormParameter
//    getFormParameter
//    getFormParameterList
//    vertxHttpServerRequest
//    normalizedPath
//    peerCertificateChain
//    absoluteURI
//    bodyHandler
//    bodyHandler
//    netSocket
//    expectMultiPart
//    expectMultiPart
//    uploadHandler
//    uploadHandler
//    formAttributes
//    exceptionHandler
//    exceptionHandler
//    dataHandler
//    dataHandler
//    pause
//    pause
//    uri
//    headers
//    endHandler
//    endHandler
//    setBody
//    version
//    body
//    getCookie
//    files
//    is
//    contentLength
//    params
//    localAddress
//    remoteAddress
//    get
//    get
//    put
//    access$000
//    resume
//    resume
//    path
//    getFile
//    query
//    method
//    setMethod

            for (Method m : YokeRequest.class.getMethods()) {
                System.err.println(m.getName());

                if ("params".equals(m.getName())) {
                    fns.put(m.getName(), new FunctionObject(m.getName(), m, this));
                }
//                fns.put(m.getName(), wrapFunction(m));
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
            // then functions
            if (fns.containsKey(name)) {
                return fns.get(name);
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
            if (fns.containsKey(name)) {
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
            Set<String> ids = fns.keySet();
            ids.addAll(context.keySet());

            return ids.toArray();
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

    private static class JSYokeResponse extends YokeResponse implements Scriptable {

        public JSYokeResponse(HttpServerResponse response, Map<String, Object> context, Map<String, Engine> engines) {
            super(response, context, engines);
        }

        @Override
        public String getClassName() {
            return null;
        }

        @Override
        public Object get(String name, Scriptable start) {
            return null;
        }

        @Override
        public Object get(int index, Scriptable start) {
            return null;
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
            return null;
        }

        @Override
        public boolean hasInstance(Scriptable instance) {
            return false;
        }
    }

    @Override
    public YokeRequest wrap(HttpServerRequest request, boolean secure, Map<String, Object> context, Map<String, Engine> engines, SessionStore store) {
        return new JSYokeRequest(request, new JSYokeResponse(request.response(), context, engines), secure, context, store);
    }
}
