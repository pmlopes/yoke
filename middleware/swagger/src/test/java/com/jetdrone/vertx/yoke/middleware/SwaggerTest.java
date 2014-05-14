package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.annotations.*;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;
import static org.vertx.testtools.VertxAssert.assertEquals;

public class SwaggerTest extends TestVerticle {

    @Api(path = "/hello", description = "Hello web service")
    @Produces("application/json")
    public static class TestSwagger {
        @GET("/hello/:name")
        @ApiDoc(
                summary = "say hello to user name",
                notes = {"note #1", "note #2"},
                parameters = {
                        @Parameter(name = "name", description = "User name", required = true)
                },
                responseMessages = {
                        @ResponseMessage(code = 200, message = "No error!")
                }
        )
        public void sayHello(YokeRequest request, Handler<Object> next) {
            request.response().end("Hello " + request.getParameter("name") + "!");
        }
    }

    @Test
    public void testAnnotatedSwagger() {
        Yoke yoke = new Yoke(this);
        Router router = new Router();
        yoke.use(router);

        final TestSwagger testSwagger = new TestSwagger();

        Swagger.from(router, "1.0.0", testSwagger).setInfo(
                new JsonObject().putString("title", "Swagger Sample App")
        );

        Router.from(router, testSwagger);

        final YokeTester tester = new YokeTester(vertx, yoke);

        tester.request("GET", "/api-docs", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                System.out.println(resp.body.toString());

                tester.request("GET", "/api-docs/hello", new Handler<Response>() {
                    @Override
                    public void handle(Response resp) {
                        assertEquals(200, resp.getStatusCode());
                        System.out.println(resp.body.toString());

                        testComplete();
                    }
                });
            }
        });
    }
}
