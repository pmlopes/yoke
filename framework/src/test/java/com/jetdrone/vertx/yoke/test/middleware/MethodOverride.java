package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.impl.CaseInsensitiveMultiMap;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;
import static org.vertx.testtools.VertxAssert.assertEquals;

public class MethodOverride extends TestVerticle {

    @Test
    public void testOverride() {

        YokeTester yoke = new YokeTester(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.MethodOverride());
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                assertEquals("DELETE", request.method());
                request.response().end();
            }
        });

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("x-http-setMethod-override", "DELETE");

        yoke.request("GET", "/upload", headers, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testOverrideUrlPost() {

        YokeTester yoke = new YokeTester(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BodyParser());
        yoke.use(new com.jetdrone.vertx.yoke.middleware.MethodOverride());
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                assertEquals("DELETE", request.method());
                request.response().end();
            }
        });

        Buffer body = new Buffer("_method=delete");

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/x-www-form-urlencoded");
        headers.add("content-length", Integer.toString(body.length()));

        yoke.request("POST", "/upload", headers, true, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testOverrideJsonPost() {

        final JsonObject json = new JsonObject().putString("_method", "delete");

        YokeTester yoke = new YokeTester(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BodyParser());
        yoke.use(new com.jetdrone.vertx.yoke.middleware.MethodOverride());
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                assertEquals("DELETE", request.method());
                request.response().end();
            }
        });

        Buffer body = new Buffer(json.encode());

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/json");
        headers.add("content-length", Integer.toString(body.length()));

        yoke.request("POST", "/upload", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }
}
