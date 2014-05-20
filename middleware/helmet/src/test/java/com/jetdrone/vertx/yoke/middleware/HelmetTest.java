package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class HelmetTest extends TestVerticle {

    @Test
    public void testCacheControl() {
        final Yoke app = new Yoke(this);
        app.use(new CacheControl());
        app.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().end("hello");
            }
        });

        new YokeTester(app).request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response response) {
                assertEquals(response.headers().get("Cache-Control"), "no-store, no-cache");
                testComplete();
            }
        });
    }

    @Test
    public void testContentTypeOptions() {
        final Yoke app = new Yoke(this);
        app.use(new ContentTypeOptions());
        app.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().end("hello");
            }
        });

        new YokeTester(app).request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response response) {
                assertEquals(response.headers().get("X-Content-Type-Options"), "nosniff");
                testComplete();
            }
        });
    }

    @Test
    public void testCrossDomain() {
        final Yoke app = new Yoke(this);
        app.use(new CrossDomain());
        app.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().end("hello");
            }
        });

        final YokeTester tester = new YokeTester(app);

        tester.request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response response) {
                assertEquals(response.body.toString(), "hello");

                tester.request("GET", "/crossdomain.xml", new Handler<Response>() {
                    @Override
                    public void handle(Response response) {
                        assertEquals(response.headers().get("Content-Type"), "text/x-cross-domain-policy");
                        assertEquals(response.body.toString(), "<?xml version=\"1.0\"?>" +
                                "<!DOCTYPE cross-domain-policy SYSTEM \"http://www.adobe.com/xml/dtds/cross-domain-policy.dtd\">" +
                                "<cross-domain-policy>" +
                                "<site-control permitted-cross-domain-policies=\"none\"/>" +
                                "</cross-domain-policy>");

                        testComplete();
                    }
                });
            }
        });
    }

    @Test
    public void testIENoOpen() {
        final Yoke app = new Yoke(this);
        app.use(new IENoOpen());
        app.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().putHeader("Content-Disposition", "attachment; filename=somefile.txt");
                request.response().end("hello");
            }
        });

        new YokeTester(app).request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response response) {
                assertEquals(response.headers().get("X-Download-Options"), "noopen");
                testComplete();
            }
        });
    }

}
