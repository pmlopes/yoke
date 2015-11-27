package com.jetdrone.vertx;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.AuthHandler;
import com.jetdrone.vertx.yoke.middleware.FormAuth;
import com.jetdrone.vertx.yoke.middleware.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import javax.crypto.Mac;

public class FormAuthExample extends AbstractVerticle {

  @Override
  public void start() {

    final Yoke app = new Yoke(this);
    app.secretSecurity("keyboard cat");

    final Mac hmac = app.security().getMac("HmacSHA256");

    app.use(new BodyParser());
    app.use(new CookieParser(hmac));
    app.use(new Session(hmac));

    final FormAuth formAuth = new FormAuth(new AuthHandler() {
      @Override
      public void handle(String username, String password, Handler<JsonObject> result) {
        if ("foo".equals(username) && "bar".equals(password)) {
          result.handle(new JsonObject().put("username", "foo"));
        } else {
          result.handle(null);
        }
      }
    });

    app.use(formAuth);

    app.use(new Router() {{
      get("/", new Handler<YokeRequest>() {
        @Override
        public void handle(YokeRequest request) {
          JsonObject session = request.get("session");

          if (session != null && session.getString("user") != null) {
            request.response().setContentType("text/html");
            request.response().end("Welcome " + session.getString("user") + "<br>" + "<a href='/logout'>logout</a>");
          } else {
            request.response().end("<a href='/login'> Login</a>");
          }
        }
      });

      get("/profile", formAuth.RequiredAuth, new Middleware() {
        @Override
        public void handle(YokeRequest request, Handler<Object> next) {
          JsonObject session = request.get("session");
          request.response().setContentType("text/html");
          request.response().end("Profile page of " + session.getString("user") + "<br>" + " click to <a href='/logout'>logout</a>");
        }
      });
    }});

    app.listen(8000);
  }
}