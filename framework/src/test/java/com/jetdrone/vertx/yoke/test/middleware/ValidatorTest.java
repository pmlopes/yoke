package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.*;
import com.jetdrone.vertx.yoke.middleware.Router;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import com.jetdrone.vertx.yoke.util.validation.Type;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static com.jetdrone.vertx.yoke.util.Validator.that;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

public class ValidatorTest extends TestVerticle {

    @Test
    public void testParam() {
        final Yoke yoke = new Yoke(this);

        yoke.use(new Router().get("/search/:from/:to", new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                com.jetdrone.vertx.yoke.util.Validator validator = new com.jetdrone.vertx.yoke.util.Validator(
                    that("param:from").is(Type.DateTime),
                    that("param:to").is(Type.DateTime)
                );

                if (!validator.isValid(request)) {
                    next.handle(400);
                    return;
                }

                request.response().end();
            }
        }));

        new YokeTester(yoke).request("GET", "/search/2012-07-14T00:00:00Z/2013-07-14T00:00:00Z", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());

                new YokeTester(yoke).request("GET", "/search/from/to", new Handler<Response>() {
                    @Override
                    public void handle(Response resp) {
                        assertEquals(400, resp.getStatusCode());
                        testComplete();
                    }
                });
            }
        });
    }

    @Test
    public void testJsonBodyValidator() {

        final JsonObject json = new JsonObject().putObject("user", new JsonObject().putString("login", "paulo").putString("password", "pwd"));

        Yoke yoke = new Yoke(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BodyParser());
        yoke.use(new Router().post("/search/:from/:to", new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                com.jetdrone.vertx.yoke.util.Validator validator = new com.jetdrone.vertx.yoke.util.Validator(
                    that("param:from").is(Type.DateTime),
                    that("param:to").is(Type.DateTime),
                    that("body:user.login").exists(),
                    that("body:user.login").is(Type.String),
                    that("body:user.password").exists(),
                    that("body:user.password").is(Type.String)
                );

                if (!validator.isValid(request)) {
                    next.handle(400);
                    return;
                }

                request.response().end();
            }
        }));

        Buffer body = new Buffer(json.encode());

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/json");
        headers.add("content-length", Integer.toString(body.length()));

        new YokeTester(yoke).request("POST", "/search/2012-07-14T00:00:00Z/2013-07-14T00:00:00Z", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testJsonBodyValidatorOptional() {

        final JsonObject json = new JsonObject().putObject("user", new JsonObject());

        Yoke yoke = new Yoke(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BodyParser());
        yoke.use(new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {

                com.jetdrone.vertx.yoke.util.Validator validator = new com.jetdrone.vertx.yoke.util.Validator(
                    that("body:user.?login").is(Type.String)
                );

                if (!validator.isValid(request)) {
                    next.handle(400);
                    return;
                }

                request.response().end();
            }
        });

        Buffer body = new Buffer(json.encode());

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/json");
        headers.add("content-length", Integer.toString(body.length()));

        new YokeTester(yoke).request("POST", "/", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testJsonBodyValidatorRequired() {

        final JsonObject json = new JsonObject().putObject("user", new JsonObject());

        Yoke yoke = new Yoke(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BodyParser());
        yoke.use(new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                com.jetdrone.vertx.yoke.util.Validator validator = new com.jetdrone.vertx.yoke.util.Validator(
                    that("body:user.?login").is(Type.String)
                );

                if (!validator.isValid(request)) {
                    next.handle(400);
                    return;
                }

                request.response().end();
            }
        });

        Buffer body = new Buffer(json.encode());

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/json");
        headers.add("content-length", Integer.toString(body.length()));

        new YokeTester(yoke).request("POST", "/", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }
}
