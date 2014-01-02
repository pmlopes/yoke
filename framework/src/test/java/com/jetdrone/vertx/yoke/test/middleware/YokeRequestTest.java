package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class YokeRequestTest extends TestVerticle {

    @Test
    public void testAccept() {
        final YokeTester yoke = new YokeTester(this);
        yoke.use(new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                assertNotNull(request.accepts("application/json"));
                testComplete();
            }
        });

        // make a new request to / with cookie should return again the same cookie
        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

        yoke.request("GET", "/", headers, null);
    }

    @Test
    public void testNormalizedPath() {
        final YokeTester yoke = new YokeTester(this);
        yoke.use(new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                assertEquals("/pom.xml", request.normalizedPath());
                testComplete();
            }
        });

        yoke.request("GET", "/./me/../pom.xml", null);
    }
}
