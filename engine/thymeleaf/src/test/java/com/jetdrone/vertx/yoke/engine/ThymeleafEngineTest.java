package com.jetdrone.vertx.yoke.engine;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class ThymeleafEngineTest extends TestVerticle {

    @Test
    public void testEngine() {
        Yoke yoke = new Yoke(this);
        yoke.engine("html", new ThymeleafEngine("views"));
        yoke.use(new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                request.put("home.welcome", "Hi there!");
                request.response().render("template.html");
            }
        });

        new YokeTester(yoke).request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }
}
