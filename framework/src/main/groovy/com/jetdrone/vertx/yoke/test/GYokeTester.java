package com.jetdrone.vertx.yoke.test;

import com.jetdrone.vertx.yoke.GYoke;
import com.jetdrone.vertx.yoke.core.GMultiMap;
import groovy.lang.Closure;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;

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

//    public void request(final String method, final String url, final GMultiMap headers, final Closure<Response> handler) {
//        request(method, url, headers, new Handler<Response>() {
//            @Override
//            public void handle(Response event) {
//                handler.call(event);
//            }
//        });
//    }
//
//    public void request(final String method, final String url, final GMultiMap headers, final Buffer body, final Closure<Response> handler) {
//        request(method, url, headers, body, new Handler<Response>() {
//            @Override
//            public void handle(Response event) {
//                handler.call(event);
//            }
//        });
//    }
}
