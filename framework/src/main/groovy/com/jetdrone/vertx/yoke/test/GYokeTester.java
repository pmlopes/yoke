/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.test;

import com.jetdrone.vertx.yoke.GYoke;
import groovy.lang.Closure;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GYokeTester extends YokeTester {

    public GYokeTester(Vertx vertx, GYoke yoke, boolean fakeSSL) {
        super(vertx, yoke.toJavaYoke(), fakeSSL);
    }

    public GYokeTester(Vertx vertx, GYoke yoke) {
        this(vertx, yoke, false);
    }

    public void request(final String method, final String url, final Closure<Response> handler) {
        request(method, url, new Handler<Response>() {
            @Override
            public void handle(Response event) {
                handler.call(event);
            }
        });
    }

    public void request(final String method, final String url, final Map<String, Object> headers, final Closure<Response> handler) {
        request(method, url, toMultiMap(headers), new Handler<Response>() {
            @Override
            public void handle(Response event) {
                handler.call(event);
            }
        });
    }

    public void request(final String method, final String url, final Map<String, Object> headers, final Buffer body, final Closure<Response> handler) {
        request(method, url, toMultiMap(headers), body, new Handler<Response>() {
            @Override
            public void handle(Response event) {
                handler.call(event);
            }
        });
    }

    private static MultiMap toMultiMap(Map<String, Object> headers) {
        if (headers == null) {
            return null;
        }

        MultiMap multiMap = new CaseInsensitiveMultiMap();

        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            Object o = entry.getValue();
            if (o != null) {
                if (o instanceof List) {
                    List<String> entries = new ArrayList<>();
                    for (Object v : (List) o) {
                        if (v != null) {
                            entries.add(v.toString());
                        }
                    }
                    multiMap.add(entry.getKey(), entries);
                    continue;
                }
                if (o instanceof String) {
                    multiMap.add(entry.getKey(), (String) o);
                }
            }
        }

        return multiMap;
    }
}
