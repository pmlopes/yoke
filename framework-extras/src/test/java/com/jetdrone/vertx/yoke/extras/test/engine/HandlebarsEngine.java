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

public class HandlebarsEngine extends TestVerticle {

    @Test
    public void testEngine() {
        try {
            // create a temp template
            File temp = File.createTempFile("template", ".html");
            FileOutputStream out = new FileOutputStream(temp);
            out.write("Hello {{name}}!".getBytes());
            out.close();
            final String location = temp.getAbsolutePath();

            YokeTester yoke = new YokeTester(this);
            yoke.engine("html", new com.jetdrone.vertx.yoke.extras.engine.HandlebarsEngine());
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
                    assertEquals("Hello Paulo!", resp.body.toString());
                    testComplete();
                }
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testEngine2() {
        try {
            // create a temp template
            File temp = File.createTempFile("template", ".html");
            FileOutputStream out = new FileOutputStream(temp);
            out.write("<html><body><ul>{{#blogs}}<li>{{name}}</li>{{/blogs}}</ul></body></html>".getBytes());
            out.close();
            final String location = temp.getAbsolutePath();

            YokeTester yoke = new YokeTester(this);
            yoke.engine("html", new com.jetdrone.vertx.yoke.extras.engine.HandlebarsEngine());
            yoke.use(new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
                    List<Map> blogs = new ArrayList<>();
                    Map<String, String> blog1 = new HashMap<>();
                    blog1.put("name", "Handlebars.java");
                    blogs.add(blog1);

                    Map<String, String> blog2 = new HashMap<>();
                    blog2.put("name", "Handlebars.js");
                    blogs.add(blog2);

                    Map<String, String> blog3 = new HashMap<>();
                    blog3.put("name", "Mustache");
                    blogs.add(blog3);

                    request.put("blogs", blogs);
                    request.response().render(location, next);
                }
            });

            yoke.request("GET", "/", new Handler<Response>() {
                @Override
                public void handle(Response resp) {
                    assertEquals(200, resp.getStatusCode());
                    assertEquals("<html><body><ul><li>Handlebars.java</li><li>Handlebars.js</li><li>Mustache</li></ul></body></html>", resp.body.toString());
                    testComplete();
                }
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
