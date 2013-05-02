package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeHttpServerRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.CaseInsensitiveMultiMap;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class BasicAuth extends TestVerticle {

    @Test
    public void testLimit() {
        final YokeTester yoke = new YokeTester(vertx);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BasicAuth("Aladdin", "open sesame"));
        yoke.use(new Middleware() {
            @Override
            public void handle(YokeHttpServerRequest request, Handler<Object> next) {
                request.response().end();
            }
        });

        // first time is forbidden
        yoke.request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(401, resp.getStatusCode());
                assertNotNull(resp.headers.get("www-authenticate"));

                // second time send the authorization header
                MultiMap headers = new CaseInsensitiveMultiMap();
                headers.add("authorization", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");

                yoke.request("GET", "/", headers, new Handler<Response>() {
                    @Override
                    public void handle(Response resp) {
                        assertEquals(200, resp.getStatusCode());
                        testComplete();
                    }
                });
            }
        });
    }
}
