package com.jetdrone.vertx.yoke.test;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class MiddlewareTest extends TestVerticle {

    @Test
    public void testMiddleware() {
        final YokeTester yoke = new YokeTester(vertx);
        yoke.use(new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                assertNotNull(this.vertx);
                testComplete();
            }
        });

        yoke.request("GET", "/", null);
    }
}
