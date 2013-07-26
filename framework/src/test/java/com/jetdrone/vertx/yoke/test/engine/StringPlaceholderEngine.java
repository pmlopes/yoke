package com.jetdrone.vertx.yoke.test.engine;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.engine.Function;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

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

            YokeTester yoke = new YokeTester(this);
            yoke.engine("html", new com.jetdrone.vertx.yoke.engine.StringPlaceholderEngine());
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
    public void testEngineFunctions() {
        try {
            // create a temp template
            File temp = File.createTempFile("template", ".html");
            FileOutputStream out = new FileOutputStream(temp);
            out.write("Hello ${fnName('Lopes')}!".getBytes());
            out.close();
            final String location = temp.getAbsolutePath();

            YokeTester yoke = new YokeTester(this);
            yoke.set("fnName", new Function() {
                @Override
                public String exec(Map<String, Object> context, Object... args) {
                    return "Paulo " + args[0];
                }
            });
            yoke.engine("html", new com.jetdrone.vertx.yoke.engine.StringPlaceholderEngine());
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
                    assertEquals("Hello Paulo Lopes!", resp.body.toString());
                    testComplete();
                }
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

//    @Test
//    public void testRG() {
//        String funcName = "([a-zA-Z0-9]+)";
//        String arguments = "\\((.*)\\)";
//        Pattern FUNCTION = Pattern.compile(funcName + "\\s*" + arguments);
//
////        Matcher f = FUNCTION.matcher("func()");
//        Matcher f = FUNCTION.matcher("func(\"arg\")");
////        Matcher f = FUNCTION.matcher("func(\"arg\", \"arg2\")");
//        if (f.find()) {
//            System.out.println("It is a function");
//
//            String argument = "(.*?)";
//            String quote = "\"";
//            String sep = "(,\\s*)?";
//            Pattern ARG = Pattern.compile(quote + argument + quote + sep);
//
//            Matcher a = ARG.matcher(f.group(2));
//
//            while (a.find()) {
//                System.out.println("It has argument: " + a.group(1));
//            }
//        }
//
//        testComplete();
//    }
}
