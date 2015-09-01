package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.Static;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Ignore;
import org.junit.Test;
import io.vertx.core.Handler;
import io.vertx.core.file.impl.PathAdjuster;
import io.vertx.core.impl.VertxInternal;
import org.vertx.testtools.TestVerticle;

import java.io.File;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

public class StaticTest extends TestVerticle {

    @Test
    public void testStaticSimple() {

        Yoke yoke = new Yoke(this);
        yoke.use(new Static("static"));

        new YokeTester(yoke).request("GET", "/dir1/file.1", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testStaticSimpleNotFound() {

        Yoke yoke = new Yoke(this);
        yoke.use(new Static("static"));

        new YokeTester(yoke).request("GET", "/dir1/file.2", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(404, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testStaticEscape() {
        Yoke yoke = new Yoke(this);
        yoke.use(new Static("static"));

        new YokeTester(yoke).request("GET", "/dir1/new%20file.1", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }
}
