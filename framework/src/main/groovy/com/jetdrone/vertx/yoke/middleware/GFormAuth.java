/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import groovy.lang.Closure;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class GFormAuth extends FormAuth {

    public GFormAuth(final Closure authHandler) {
        super(new AuthHandler() {
            @Override
            public void handle(String username, String password, Handler<JsonObject> result) {
                // TODO: Groovy works with Map not JsonObject
                authHandler.call(username, password, result);
            }
        });
    }

    public GFormAuth(boolean forceSSL, final Closure authHandler) {
        super(forceSSL, new AuthHandler() {
            @Override
            public void handle(String username, String password, Handler<JsonObject> result) {
                // TODO: Groovy works with Map not JsonObject
                authHandler.call(username, password, result);
            }
        });
    }

    public GFormAuth(boolean forceSSL, String loginURI, String logoutURI, String loginTemplate, final Closure authHandler) {
        super(forceSSL, loginURI, logoutURI, loginTemplate, new AuthHandler() {
            @Override
            public void handle(String username, String password, Handler<JsonObject> result) {
                // TODO: Groovy works with Map not JsonObject
                authHandler.call(username, password, result);
            }
        });
    }
}
