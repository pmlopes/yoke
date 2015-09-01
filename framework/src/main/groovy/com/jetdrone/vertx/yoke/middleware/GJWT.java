package com.jetdrone.vertx.yoke.middleware;

import groovy.lang.Closure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class GJWT extends JWT {
    public GJWT() {
        super();
    }

    public GJWT(final @NotNull String skip) {
        super(skip);
    }

    public GJWT(final @NotNull String skip, final @NotNull Closure closure) {
        super(skip, new JWTHandler() {
            @Override
            public void handle(JsonObject token, Handler<Object> result) {
                closure.call(token.toMap(), result);
            }
        });
    }

    public GJWT(final @NotNull Closure closure) {
        super(new JWTHandler() {
            @Override
            public void handle(JsonObject token, Handler<Object> result) {
                closure.call(token.toMap(), result);
            }
        });
    }
}
