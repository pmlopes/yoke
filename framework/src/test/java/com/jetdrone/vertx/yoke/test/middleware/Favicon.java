package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.util.Utils;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class Favicon extends TestVerticle {

    @Test
    public void testFavicon() {
        YokeTester yoke = new YokeTester(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.Favicon());

        final Buffer icon = Utils.readResourceToBuffer(com.jetdrone.vertx.yoke.middleware.Favicon.class, "favicon.ico");

        yoke.request("GET", "/favicon.ico", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                assertArrayEquals(icon.getBytes(), resp.body.getBytes());
                testComplete();
            }
        });
    }
}
