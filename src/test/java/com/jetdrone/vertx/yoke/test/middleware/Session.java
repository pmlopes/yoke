package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.middleware.CookieParser;
import com.jetdrone.vertx.yoke.middleware.Router;
import com.jetdrone.vertx.yoke.middleware.YokeHttpServerRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.CaseInsensitiveMultiMap;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class Session extends TestVerticle {

    @Test
    public void testSession() {
        final YokeTester yoke = new YokeTester(vertx);
        yoke.use(new CookieParser());
        yoke.use(new com.jetdrone.vertx.yoke.middleware.Session("keyboard.cat"));
        yoke.use(new Router() {{
            get("/", new Handler<YokeHttpServerRequest>() {
                @Override
                public void handle(YokeHttpServerRequest request) {
                    request.response().end();
                }
            });
            get("/new", new Handler<YokeHttpServerRequest>() {
                @Override
                public void handle(YokeHttpServerRequest request) {
                    request.setSessionId("1");
                    request.response().end();
                }
            });
            get("/delete", new Handler<YokeHttpServerRequest>() {
                @Override
                public void handle(YokeHttpServerRequest request) {
                    request.setSessionId(null);
                    request.response().end();
                }
            });
        }});

        yoke.request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                // start: there is no cookie
                assertEquals(200, resp.getStatusCode());
                String nocookie = resp.headers.get("set-cookie");
                assertNull(nocookie);

                // create session
                yoke.request("GET", "/new", new Handler<Response>() {
                    @Override
                    public void handle(Response resp) {
                        // start: there is a cookie
                        assertEquals(200, resp.getStatusCode());
                        final String cookie = resp.headers.get("set-cookie");
                        assertNotNull(cookie);

                        // make a new request to / with cookie should return again the same cookie
                        MultiMap headers = new CaseInsensitiveMultiMap();
                        headers.add("cookie", cookie);

                        yoke.request("GET", "/", headers, new Handler<Response>() {
                            @Override
                            public void handle(Response resp) {
                                // the session should be the same, so no set-cookie
                                assertEquals(200, resp.getStatusCode());
                                String nocookie = resp.headers.get("set-cookie");
                                assertNull(nocookie);

                                // end the session
                                MultiMap headers = new CaseInsensitiveMultiMap();
                                headers.add("cookie", cookie);

                                yoke.request("GET", "/delete", headers, new Handler<Response>() {
                                    @Override
                                    public void handle(Response resp) {
                                        // there should be a set-cookie with maxAge 0
                                        assertEquals(200, resp.getStatusCode());
                                        String cookie = resp.headers.get("set-cookie");
                                        assertNotNull(cookie);

                                        assertTrue(cookie.startsWith("yoke.sess=;"));
                                        testComplete();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
}
