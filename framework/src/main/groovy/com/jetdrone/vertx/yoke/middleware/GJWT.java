package com.jetdrone.vertx.yoke.middleware;

import groovy.lang.Closure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

public class GJWT extends JWT {
    public GJWT(final @NotNull String keyPassword) {
        super(keyPassword);
    }

    public GJWT(final @NotNull String keyPassword, final @Nullable String skip) {
        super(keyPassword, skip);
    }

    public GJWT(final @NotNull String keyPassword, final @Nullable String skip, final Closure closure) {
        super(keyPassword, skip, new JWTHandler() {
            @Override
            public void handle(JsonObject token, Handler<Object> result) {
                closure.call(token.toMap(), result);
            }
        });
    }
}
