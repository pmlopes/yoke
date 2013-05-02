package com.jetdrone.vertx.yoke.test.engine;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeHttpServerRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import java.io.File;
import java.io.FileOutputStream;

import static org.vertx.testtools.VertxAssert.*;

public class StringPlaceholderEngine extends TestVerticle {

    @Test
    public void testEngine() {

        try {
            // create a temp template
            File temp = File.createTempFile("template", ".html");
            FileOutputStream out = new FileOutputStream(temp);
            out.write("Hello ${name}!".getBytes());
            out.close();
            final String location = temp.getAbsolutePath();

            YokeTester yoke = new YokeTester(vertx);
            yoke.engine("html", new com.jetdrone.vertx.yoke.engine.StringPlaceholderEngine());
            yoke.use(new Middleware() {
                @Override
                public void handle(YokeHttpServerRequest request, Handler<Object> next) {
                    request.put("name", "Paulo");
                    request.response().render(location, next);
                }
            });

            yoke.request("GET", "/", new Handler<Response>() {
                @Override
                public void handle(Response resp) {
                    assertEquals(200, resp.getStatusCode());
                    assertEquals("Hello Paulo!", resp.body.toString());
                    testComplete();
                }
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
