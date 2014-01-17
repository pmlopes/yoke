package com.jetdrone.vertx.yoke.extras.test.engine;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.vertx.testtools.VertxAssert.*;

public class Jade4JEngine extends TestVerticle {

    @Test
    public void testEngine() {
        try {
            // create a temp template
            File temp = File.createTempFile("template", ".jade");
            FileOutputStream out = new FileOutputStream(temp);
            out.write("!!!\nhtml\n  head\n  body".getBytes());
            out.close();
            final String location = temp.getAbsolutePath();

            Yoke yoke = new Yoke(this);
            yoke.engine("jade", new com.jetdrone.vertx.yoke.extras.engine.Jade4JEngine());
            yoke.use(new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
                    request.response().render(location, next);
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
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
