package com.jetdrone.vertx.yoke.extras.test.engine;

import com.jetdrone.vertx.yoke.Middleware;
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

public class MVELEngine extends TestVerticle {

    @Test
    public void testBasicObjectAccess() {
        try {
            // create a temp template
            File temp = File.createTempFile("template", ".html");
            FileOutputStream out = new FileOutputStream(temp);
            out.write("<h1>@{name}</h1>".getBytes());
            out.close();
            final String location = temp.getAbsolutePath();

            YokeTester yoke = new YokeTester(this);
            yoke.engine("html", new com.jetdrone.vertx.yoke.extras.engine.MVELEngine());
            yoke.use(new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
                    request.put("name", "Paulo");
                    request.response().render(location, next);
                }
            });

            yoke.request("GET", "/", new Handler<Response>() {
                @Override
                public void handle(Response resp) {
                    assertEquals(200, resp.getStatusCode());
                    assertEquals("<h1>Paulo</h1>", resp.body.toString());
                    testComplete();
                }
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSimpleIteration() {
        try {
            // create a temp template
            File temp = File.createTempFile("template", ".html");
            FileOutputStream out = new FileOutputStream(temp);
            out.write(("<p>@foreach{index : alphabetical}<a href=\"@{index.uri}\">@{index.description}</a>@end{}</p>").getBytes());
            out.close();
            final String location = temp.getAbsolutePath();

            YokeTester yoke = new YokeTester(this);
            yoke.engine("html", new com.jetdrone.vertx.yoke.extras.engine.MVELEngine());
            yoke.use(new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
                    List<Map<String, String>> list = new ArrayList<>();
                    Map<String, String> item = new HashMap<>();
                    item.put("uri", "a");
                    item.put("description", "b");
                    list.add(item);

                    item = new HashMap<>();
                    item.put("uri", "c");
                    item.put("description", "d");
                    list.add(item);

                    request.put("alphabetical", list);
                    request.response().render(location, next);
                }
            });

            yoke.request("GET", "/", new Handler<Response>() {
                @Override
                public void handle(Response resp) {
                    assertEquals(200, resp.getStatusCode());
                    assertEquals("<p><a href=\"a\">b</a><a href=\"c\">d</a></p>", resp.body.toString());
                    testComplete();
                }
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
