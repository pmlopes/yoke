package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.impl.CaseInsensitiveMultiMap;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class RequestFunctions extends TestVerticle {

    @Test
    public void testAccepts() {
        YokeTester yoke = new YokeTester(this);
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().end(request.accepts("text"));
            }
        });

        // second time send the authorization header
        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("accept", "text/plain; q=0.5, application/json, text/html; q=0.8, text/xml");
        // expected order is:
        // application/json
        // text/xml
        // text/html
        // text/plain

        yoke.request("GET", "/", headers, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                assertEquals(resp.body.toString(), "text/xml");
                testComplete();
            }
        });
    }

    @Test
    public void testIp() {
        YokeTester yoke = new YokeTester(this);
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                assertEquals("123.456.123.456", request.ip());
                testComplete();
            }
        });

        // second time send the authorization header
        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("x-forward-for", "123.456.123.456, 111.111.11.11");

        yoke.request("GET", "/", headers, null);
    }
}
