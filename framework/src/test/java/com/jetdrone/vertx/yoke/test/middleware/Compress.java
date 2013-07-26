package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.impl.CaseInsensitiveMultiMap;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

public class Compress extends TestVerticle {
  @Test
  public void testGzipCompress() {
    YokeTester yoke = new YokeTester(this);
    yoke.use(new com.jetdrone.vertx.yoke.middleware.Compress());
    yoke.use(new Middleware() {
      @Override
      public void handle(YokeRequest request, Handler<Object> next) {
        request.response().end(new JsonObject().putString("hello", "world"));
      }
    });

    MultiMap headers = new CaseInsensitiveMultiMap();
    headers.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    headers.add("Accept-Encoding", "gzip,deflate,sdch");

    yoke.request("GET", "/", headers, new Handler<Response>() {
      @Override
      public void handle(Response resp) {
        assertEquals(200, resp.getStatusCode());
        for (int i = 0; i < resp.body.length(); i++) {
            System.out.println((int) resp.body.getByte(i));
        }
        testComplete();
      }
    });
  }

//  @Test
//  public void testGZip() {
//    Yoke yoke = new Yoke(vertx);
//    yoke.use(new com.jetdrone.vertx.yoke.middleware.Compress());
//    yoke.use(new Middleware() {
//      @Override
//      public void handle(YokeRequest request, Handler<Object> next) {
//        request.response().setContentType("text/plain", "UTF-8");
//        request.response().setChunked(true);
//
//        request.response().write("hello0000000000000000000000000000000000000000000000000\n");
//        request.response().write("hello0000000000000000000000000000000000000000000000000\n");
//        request.response().write("hello0000000000000000000000000000000000000000000000000\n");
//        request.response().write("hello0000000000000000000000000000000000000000000000000\n");
//        request.response().write("hello0000000000000000000000000000000000000000000000000\n");
//        request.response().write("hello0000000000000000000000000000000000000000000000000\n");
//        request.response().write("hello0000000000000000000000000000000000000000000000000\n");
//        request.response().write("hello0000000000000000000000000000000000000000000000000\n");
//        request.response().write("hello0000000000000000000000000000000000000000000000000\n");
//        request.response().write("hello0000000000000000000000000000000000000000000000000\n");
//        request.response().write("hello0000000000000000000000000000000000000000000000000\n");
//        request.response().end("hello0000000000000000000000000000000000000000000000000\n");
//
//
//      }
//    });
//
//    yoke.listen(8888);
//  }
}
