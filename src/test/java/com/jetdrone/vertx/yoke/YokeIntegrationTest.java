package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.middleware.*;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class YokeIntegrationTest extends TestVerticle {

    @Test
    public void testResponseTime() {
        Yoke yoke = new Yoke(vertx);
        yoke.use(new ResponseTime());
        yoke.use(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest request) {
                request.response().end();
            }
        });
        yoke.listen(8181);

        vertx.createHttpClient().setPort(8181).getNow("/",new Handler<HttpClientResponse>() {
            @Override
            public void handle(HttpClientResponse resp) {
                assertEquals(200, resp.statusCode());
                assertNotNull(resp.headers().get("x-response-time"));
                testComplete();
            }
        });
    }

    @Test
    public void testLib() {
        Yoke yoke = new Yoke(vertx);
        yoke.use(new Static("/home/plopes", 0));
        yoke.listen(8181);
    }
}
