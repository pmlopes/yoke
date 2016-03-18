package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;

/**
 * IE8+ X-Download-Options header
 *
 * When sending attachments, don't allow people to open them in the context of
 * your site.
 *
 * For more, see this MSDN blog post:
 * http://blogs.msdn.com/b/ie/archive/2008/07/02/ie8-security-part-v-comprehensive-protection.aspx
 */
public final class IENoOpen extends Middleware {

    @Override
    public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        request.response().putHeader("X-Download-Options", "noopen");
        next.handle(null);
    }
}
