package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.CookieParser;
import com.jetdrone.vertx.yoke.middleware.Router;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.testtools.TestVerticle;

import javax.crypto.Mac;

import static org.vertx.testtools.VertxAssert.*;

public class Session extends TestVerticle {

    @Test
    public void testSession() {
        final Yoke yoke = new Yoke(this);
        yoke.secretSecurity("keyboard cat");

        final Mac hmac = yoke.security().getMac("HmacSHA256");
        yoke.use(new CookieParser(hmac));
        yoke.use(new com.jetdrone.vertx.yoke.middleware.Session(hmac));
        yoke.use(new Router() {{
            get("/", new Handler<YokeRequest>() {
                @Override
                public void handle(YokeRequest request) {
                    request.response().end();
                }
            });
            get("/new", new Handler<YokeRequest>() {
                @Override
                public void handle(YokeRequest request) {
                    request.createSession();
                    request.response().end();
                }
            });
            get("/delete", new Handler<YokeRequest>() {
                @Override
                public void handle(YokeRequest request) {
                    request.destroySession();
                    request.response().end();
                }
            });
        }});

        final YokeTester yokeAssert = new YokeTester(yoke);

        yokeAssert.request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                // start: there is no cookie
                assertEquals(200, resp.getStatusCode());
                String nocookie = resp.headers.get("set-cookie");
                assertNull(nocookie);

                // create session
                yokeAssert.request("GET", "/new", new Handler<Response>() {
                    @Override
                    public void handle(Response resp) {
                        // start: there is a cookie
                        assertEquals(200, resp.getStatusCode());
                        final String cookie = resp.headers.get("set-cookie");
                        assertNotNull(cookie);

                        // make a new request to / with cookie should return again the same cookie
                        MultiMap headers = new CaseInsensitiveMultiMap();
                        headers.add("cookie", cookie);

                        yokeAssert.request("GET", "/", headers, new Handler<Response>() {
                            @Override
                            public void handle(Response resp) {
                                // the session should be the same, so no set-cookie
                                assertEquals(200, resp.getStatusCode());
                                String nocookie = resp.headers.get("set-cookie");
                                assertNull(nocookie);

                                // end the session
                                MultiMap headers = new CaseInsensitiveMultiMap();
                                headers.add("cookie", cookie);

                                yokeAssert.request("GET", "/delete", headers, new Handler<Response>() {
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
