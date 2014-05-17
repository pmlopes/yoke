package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;

/**
 *  X-Content-Type-Options
 * The only defined value, "nosniff", prevents Internet Explorer from MIME-sniffing a response away from the declared content-type
 */
public final class ContentTypeOptions extends Middleware {

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        request.response().putHeader("X-Content-Type-Options", "nosniff");
        next.handle(null);
    }
}
