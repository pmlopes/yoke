package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.annotations.*;
import com.jetdrone.vertx.yoke.middleware.*;
import com.jetdrone.vertx.yoke.middleware.BodyParser;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import java.util.regex.Pattern;

import static org.vertx.testtools.VertxAssert.*;

public class Router extends TestVerticle {

    public static class TestRouter {
        @GET("/ws")
        public void get(YokeRequest request, Handler<Object> next) {
            request.response().end("Hello ws!");
        }
    }

    @Test
    public void testAnnotatedRouter() {
        Yoke yoke = new Yoke(this);
        yoke.use(com.jetdrone.vertx.yoke.middleware.Router.from(new TestRouter()));

        new YokeTester(yoke).request("GET", "/ws", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                assertEquals("Hello ws!", resp.body.toString());
                testComplete();
            }
        });
    }

    public static class TestRouter2 {
        @GET("/ws")
        @Produces({"text/plain"})
        public void get(YokeRequest request, Handler<Object> next) {
            request.response().end("Hello ws!");
        }
    }

    @Test
    public void testAnnotatedRouter2() {
        Yoke yoke = new Yoke(this);
        yoke.use(com.jetdrone.vertx.yoke.middleware.Router.from(new TestRouter2()));

        new YokeTester(yoke).request("GET", "/ws", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                assertEquals("Hello ws!", resp.body.toString());
                testComplete();
            }
        });
    }

    @Test
    public void testRouterWithParams() {
        Yoke yoke = new Yoke(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.Router() {{
            get("/api/:userId", new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {

                    assertNotNull(request.get("user"));
                    assertTrue(request.get("user") instanceof JsonObject);
                    request.response().end("OK");
                }
            });
            param("userId", new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
                    assertEquals("1", request.params().get("userId"));
                    // pretend that we went on some DB and got a json object representing the user
                    request.put("user", new JsonObject("{\"id\":" + request.params().get("userId") + "}"));
                    next.handle(null);
                }
            });
        }});

        new YokeTester(yoke).request("GET", "/api/1", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                assertEquals("OK", resp.body.toString());
                testComplete();
            }
        });
    }

    @Test
    public void testRouterWithRegExParamsFail() {
        Yoke yoke = new Yoke(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.Router() {{
            get("/api/:userId", new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
                    request.response().end("OK");
                }
            });
            param("userId", Pattern.compile("[1-9][0-9]"));
        }});

        // the pattern expects 2 digits
        new YokeTester(yoke).request("GET", "/api/1", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(400, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testRouterWithRegExParamsPass() {
        Yoke yoke = new Yoke(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.Router() {{
            get("/api/:userId", new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
                    request.response().end("OK");
                }
            });
            param("userId", Pattern.compile("[1-9][0-9]"));
        }});

        // the pattern expects 2 digits
        new YokeTester(yoke).request("GET", "/api/10", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testTrailingSlashes() {
        final Yoke yoke = new Yoke(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.Router() {{
            get("/api", new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
                    request.response().end("OK");
                }
            });
        }});

        final YokeTester yokeAssert = new YokeTester(yoke);

        yokeAssert.request("GET", "/api", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());

                yokeAssert.request("GET", "/api/", new Handler<Response>() {
                    @Override
                    public void handle(Response resp) {
                        assertEquals(200, resp.getStatusCode());
                        testComplete();
                    }
                });
            }
        });
    }

    @Test
    public void testDash() {
        final Yoke yoke = new Yoke(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.Router() {{
            get("/api-stable", new Middleware() {
                @Override
                public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
                    request.response().end("OK");
                }
            });
        }});

        final YokeTester yokeAssert = new YokeTester(yoke);

        yokeAssert.request("GET", "/api-stable", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }

    public static class R2 {

        @RegExParam("userId")
        public final Pattern userId = Pattern.compile("[1-9][0-9]");

        @GET("/api/:userId")
        public void handle(YokeRequest request, Handler<Object> next) {
            request.response().end("OK");
        }
    }

    @Test
    public void testRouterWithRegExAnnParamsFail() {
        Yoke yoke = new Yoke(this);
        yoke.use(com.jetdrone.vertx.yoke.middleware.Router.from(new R2()));

        // the pattern expects 2 digits
        new YokeTester(yoke).request("GET", "/api/1", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(400, resp.getStatusCode());
                testComplete();
            }
        });
    }

    public static class R3 {
        @POST("/api")
        @JsonSchema("classpath://Person.json")
        public void handle(YokeRequest request, Handler<Object> next) {
            request.response().end("OK");
        }
    }

    @Test
    public void testJsonSchemaProcessing() {
        Yoke yoke = new Yoke(this);
        yoke.use(new BodyParser());
        yoke.use(com.jetdrone.vertx.yoke.middleware.Router.from(new R3()));

        Buffer body = new Buffer("{}");

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/json");
        headers.add("content-length", Integer.toString(body.length()));

        new YokeTester(yoke).request("POST", "/api", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(400, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testJsonSchemaProcessing2() {
        Yoke yoke = new Yoke(this);
        yoke.use(com.jetdrone.vertx.yoke.middleware.Router.from(new R3()));

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/json");
        headers.add("content-length", "0");

        new YokeTester(yoke).request("POST", "/api", headers, null, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(400, resp.getStatusCode());
                testComplete();
            }
        });
    }
}
