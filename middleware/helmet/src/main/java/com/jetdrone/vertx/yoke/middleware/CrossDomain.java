package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;

public final class CrossDomain extends Middleware {

    private static final String DATA =
            "<?xml version=\"1.0\"?>" +
            "<!DOCTYPE cross-domain-policy SYSTEM \"http://www.adobe.com/xml/dtds/cross-domain-policy.dtd\">" +
            "<cross-domain-policy>" +
            "<site-control permitted-cross-domain-policies=\"none\"/>" +
            "</cross-domain-policy>";

    @Override
    public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        if ("/crossdomain.xml".equals(request.path())) {
            request.response().putHeader("Content-Type", "text/x-cross-domain-policy");
            request.response().end(DATA);
        } else {
            next.handle(null);
        }
    }
}
