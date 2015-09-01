package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;

/**
 * HTTP Strict Transport Security (HSTS)
 * http://tools.ietf.org/html/rfc6797
 */
public final class HSTS extends Middleware {

    private final String header;

    public HSTS() {
        this(false);
    }

    public HSTS(boolean includeSubDomains) {
        // ~6Months
        this(15768000, includeSubDomains);
    }

    public HSTS(long maxAge, boolean includeSubDomains) {
        if (includeSubDomains) {
            header = "max-age=" + maxAge + "; includeSubdomains";
        } else {
            header = "max-age=" + maxAge;
        }
    }

    @Override
    public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        boolean isSecure = (request.isSecure())
                || ("on".equals(request.getHeader("front-end-https")))
                || ("https".equals(request.getHeader("x-forwarded-proto")));

        if (isSecure) {
            request.response().putHeader("Strict-Transport-Security", header);
        }

        next.handle(null);
    }
}
