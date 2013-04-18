package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.middleware.*;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Verticle;

public class Tester extends Verticle {

    @Override
    public void start() {
        Yoke yoke = new Yoke(vertx);

        yoke.use(new ResponseTime());
        yoke.use(new Timeout());
        yoke.use(new Favicon());
        yoke.use(new Vhost("*.com", new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest event) {
                event.response().end("vhost");
            }
        }));
        yoke.use(new MethodOverride());
        yoke.use(new CookieParser());
        yoke.use(new Limit(100));
        yoke.use(new BodyParser());
        yoke.use(new Static("public", 0));
        yoke.use("admin", new BasicAuth("foo", "bar"));
        yoke.use(new ErrorHandler(true));

        yoke.use(new Router() {{
            all("/upload", new Handler<HttpServerRequest>() {
                @Override
                public void handle(HttpServerRequest request) {
                    System.out.println("HERE");
                    request.response().end();
                }
            });
        }});

        yoke.listen(3000);
    }
}
