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

public class BodyParser extends TestVerticle {

    @Test
    public void testJsonBodyParser() {

        final JsonObject json = new JsonObject().putString("key", "value");

        YokeTester yoke = new YokeTester(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BodyParser());
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                assertNotNull(request.jsonBody());
                assertEquals(request.jsonBody().encode(), json.encode());
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
                assertNotNull(resp.body);
                testComplete();
            }
        });
    }

    @Test
    public void testMapBodyParser() {

        YokeTester yoke = new YokeTester(this);
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                MultiMap body = request.formAttributes();
                assertEquals("value", body.get("param"));
                request.response().end();
            }
        });

        Buffer body = new Buffer("param=value");

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/x-www-form-urlencoded");
        headers.add("content-length", Integer.toString(body.length()));

        yoke.request("POST", "/upload", headers, true, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                assertNotNull(resp.body);
                testComplete();
            }
        });
    }

    @Test
    public void testBodyParserWithEmptyBody() {

        YokeTester yoke = new YokeTester(this);
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().end();
            }
        });

        yoke.request("DELETE", "/upload", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }
}
