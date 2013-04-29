package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.middleware.*;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class YokeIntegrationTest extends TestVerticle {

//    @Test
//    public void testResponseTime() {
//        Yoke yoke = new Yoke(vertx);
//        yoke.use(new ResponseTime());
//        yoke.use(new Handler<YokeHttpServerRequest>() {
//            @Override
//            public void handle(YokeHttpServerRequest request) {
//                request.response().end();
//            }
//        });
//        yoke.listen(8181);
//
//        vertx.createHttpClient().setPort(8181).getNow("/",new Handler<HttpClientResponse>() {
//            @Override
//            public void handle(HttpClientResponse resp) {
//                assertEquals(200, resp.statusCode());
//                assertNotNull(resp.headers().get("x-response-time"));
//                testComplete();
//            }
//        });
//    }
//
//    @Test
//    public void testErrorHandler() {
//        Yoke yoke = new Yoke(vertx);
//        yoke.use(new ErrorHandler(true));
//        yoke.use(new Middleware() {
//            @Override
//            public void handle(YokeHttpServerRequest request, Handler<Object> next) {
//                next.handle(new Exception());
//            }
//        });
//        yoke.listen(8181);
//
//        vertx.createHttpClient().setPort(8181).getNow("/",new Handler<HttpClientResponse>() {
//            @Override
//            public void handle(HttpClientResponse resp) {
//                assertEquals(500, resp.statusCode());
//                testComplete();
//            }
//        });
//    }

    @Test
    public void testLib() {
        Yoke yoke = new Yoke(vertx);
        yoke.use(new Session("keyboard.cat"));
        yoke.use(new Middleware() {
            @Override
            public void handle(YokeHttpServerRequest request, Handler<Object> next) {
                request.setSessionId("1");
                next.handle(null);
            }
        });
        yoke.use(new Static("/home/paulo", 0, true, false));
        yoke.listen(8181);
    }
}
