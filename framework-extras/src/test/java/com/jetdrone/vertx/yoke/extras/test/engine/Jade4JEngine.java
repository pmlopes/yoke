package com.jetdrone.vertx.yoke.extras.test.engine;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class Jade4JEngine extends TestVerticle {

    @Test
    public void testEngine() {
        Yoke yoke = new Yoke(this);
        yoke.engine(new com.jetdrone.vertx.yoke.extras.engine.Jade4JEngine("views"));
        yoke.use(new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                request.response().render("template.jade");
            }
        });

        new YokeTester(vertx, yoke).request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                assertEquals("<!DOCTYPE html><html><head></head><body></body></html>", resp.body.toString());
                testComplete();
            }
        });
    }

    @Test
    public void testEngine2() {
        Yoke yoke = new Yoke(this);
        yoke.engine(new com.jetdrone.vertx.yoke.extras.engine.Jade4JEngine("views"));
        yoke.use(new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                request.put("pageName", "Vert.X Test");
                request.response().render("template2.jade");
            }
        });

        new YokeTester(vertx, yoke).request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
//                assertEquals("<!DOCTYPE html><html><head><title>Vert.X Test</title><script src=\"static/sockjs-min-0.3.4.js\" type=\"text/javascript\"></script><script src=\"static/vertxbus.js\" type=\"text/javascript\"></script><script src=\"static/main.js\" type=\"text/javascript\"></script><link rel=\"stylesheet\" type=\"text/css\" href=\"static/main.css\"></head><body><h1>Vert.X Test</h1></body></html>", resp.body.toString());
                testComplete();
            }
        });
    }
}
