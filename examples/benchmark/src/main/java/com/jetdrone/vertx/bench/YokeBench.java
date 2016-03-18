package com.jetdrone.vertx.bench;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.BodyParser;
import com.jetdrone.vertx.yoke.middleware.Router;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Handler;

public class YokeBench extends AbstractVerticle {

  @Override
  public void start() {

    final Middleware foo = new Middleware() {
      @Override
      public void handle(YokeRequest request, Handler<Object> next) {
        next.handle(null);
      }
    };

    new Yoke(this)
        .use(new BodyParser())
        .use("/middleware", foo)
        .use("/middleware", foo)
        .use("/middleware", foo)
        .use("/middleware", foo)
        .use(new Router()
            .get("/", new Handler<YokeRequest>() {
              @Override
              public void handle(YokeRequest request) {
                request.response().end("Hello World\n");
              }
            })
            .get("/json", new Handler<YokeRequest>() {
              @Override
              public void handle(YokeRequest request) {
                request.response().end(new JsonObject().put("name", "Tobi").put("role", "admin"));
              }
            })
            .get("/middleware", new Handler<YokeRequest>() {
              @Override
              public void handle(YokeRequest request) {
                request.response().end("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
              }
            })
        ).listen(8080);
  }
}
