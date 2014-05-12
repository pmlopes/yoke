package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.security.YokeKeyStore;
import groovy.lang.Closure;
import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

public class GJWT extends JWT {
    public GJWT(@NotNull String secret) {
        super(secret);
    }

    public GJWT(@NotNull String secret, String skip) {
        super(secret, skip);
    }

    public GJWT(@NotNull String secret, String skip, final Closure closure) {
        super(secret, skip, new JWTHandler() {
            @Override
            public void handle(JsonObject token, Handler<Object> result) {
                closure.call(token.toMap(), result);
            }
        });
    }

    public GJWT(YokeKeyStore keystore, String keyPassword) {
        super(keystore, keyPassword);
    }

    public GJWT(YokeKeyStore keystore, String keyPassword, String skip) {
        super(keystore, keyPassword, skip);
    }

    public GJWT(YokeKeyStore keystore, String keyPassword, String skip, final Closure closure) {
        super(keystore, keyPassword, skip, new JWTHandler() {
            @Override
            public void handle(JsonObject token, Handler<Object> result) {
                closure.call(token.toMap(), result);
            }
        });
    }


}
