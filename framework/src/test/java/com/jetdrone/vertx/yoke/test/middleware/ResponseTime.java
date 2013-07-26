package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class ResponseTime extends TestVerticle {

    @Test
    public void testResponseTime() {
        YokeTester yoke = new YokeTester(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.ResponseTime());
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().end();
            }
        });

        yoke.request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                assertNotNull(resp.headers().get("x-response-time"));
                testComplete();
            }
        });
    }
}
