package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.impl.WebClient;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;

public final class IEXSS extends Middleware {

    private final boolean setOnOldIE;

    public IEXSS() {
        this(false);
    }

    public IEXSS(boolean setOnOldIE) {
        this.setOnOldIE = setOnOldIE;
    }

    @Override
    public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        final WebClient webClient = WebClient.detect(request.getHeader("user-agent"));

        boolean isIE = webClient.getUserAgent() == WebClient.UserAgent.IE;
        int majorVersion = webClient.getMajorVersion();

        String value;

        if ((!isIE) || (majorVersion >= 9) || (setOnOldIE)) {
            value = "1; mode=block";
        } else {
            value = "0";
        }

        request.response().putHeader("X-XSS-Protection", value);
        next.handle(null);
    }
}
