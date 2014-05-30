package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class IEXSSTest extends TestVerticle {

    public static final String IE_7 = "Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 6.0; en-US)";
    public static final String IE_8 = "Mozilla/4.0 ( ; MSIE 8.0; Windows NT 6.0; Trident/4.0; GTB6.6; .NET CLR 3.5.30729)";
    public static final String IE_9 = "Mozilla/5.0 (Windows; U; MSIE 9.0; WIndows NT 9.0; en-US)";
    public static final String FIREFOX_23 = "Mozilla/5.0 (Windows NT 6.2; rv:22.0) Gecko/20130405 Firefox/23.0";

    @Test
    public void setsHeaderForFirefox23() {
        final Yoke app = new Yoke(this);
        app.use(new IEXSS());
        app.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().end("hello");
            }
        });

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("User-Agent", FIREFOX_23);

        new YokeTester(app).request("GET", "/", headers, new Handler<Response>() {
            @Override
            public void handle(Response response) {
                assertEquals(response.headers().get("X-XSS-Protection"), "1; mode=block");
                testComplete();
            }
        });
    }

    @Test
    public void setsHeaderForIE9() {
        final Yoke app = new Yoke(this);
        app.use(new IEXSS());
        app.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().end("hello");
            }
        });

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("User-Agent", IE_9);

        new YokeTester(app).request("GET", "/", headers, new Handler<Response>() {
            @Override
            public void handle(Response response) {
                assertEquals(response.headers().get("X-XSS-Protection"), "1; mode=block");
                testComplete();
            }
        });
    }

    @Test
    public void setsHeaderTo0ForIE8() {
        final Yoke app = new Yoke(this);
        app.use(new IEXSS());
        app.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().end("hello");
            }
        });

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("User-Agent", IE_8);

        new YokeTester(app).request("GET", "/", headers, new Handler<Response>() {
            @Override
            public void handle(Response response) {
                assertEquals(response.headers().get("X-XSS-Protection"), "0");
                testComplete();
            }
        });
    }

    @Test
    public void setsHeaderTo0ForIE7() {
        final Yoke app = new Yoke(this);
        app.use(new IEXSS());
        app.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().end("hello");
            }
        });

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("User-Agent", IE_7);

        new YokeTester(app).request("GET", "/", headers, new Handler<Response>() {
            @Override
            public void handle(Response response) {
                assertEquals(response.headers().get("X-XSS-Protection"), "0");
                testComplete();
            }
        });
    }

    @Test
    public void allowsYouToSetTheHeaderForOldIE() {
        final Yoke app = new Yoke(this);
        app.use(new IEXSS(true));
        app.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().end("hello");
            }
        });

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("User-Agent", IE_8);

        new YokeTester(app).request("GET", "/", headers, new Handler<Response>() {
            @Override
            public void handle(Response response) {
                assertEquals(response.headers().get("X-XSS-Protection"), "1; mode=block");
                testComplete();
            }
        });
    }

}
