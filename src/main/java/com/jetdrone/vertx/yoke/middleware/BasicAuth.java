package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import javax.xml.bind.DatatypeConverter;

public class BasicAuth extends Middleware {

    public abstract static class AuthHandler {
        public abstract void handle(String username, String password, Handler<Boolean> result);
    }

    private final String realm;
    private final AuthHandler authHandler;

    public BasicAuth(final String username, final String password, String realm) {
        this.realm = realm;
        authHandler = new AuthHandler() {
            @Override
            public void handle(String _username, String _password, Handler<Boolean> result) {
                result.handle(username.equals(_username) && password.equals(_password));
            }
        };
    }

    public BasicAuth(String username, String password) {
        this (username, password, "Authentication required");

    }

    public BasicAuth(AuthHandler authHandler, String realm) {
        this.realm = realm;
        this.authHandler = authHandler;
    }

    public BasicAuth(AuthHandler authHandler) {
        this(authHandler, "Authentication required");
    }

    @Override
    public void handle(final HttpServerRequest request, final Handler<Object> next) {
        // inside middleware the original request has been wrapped with yoke's
        // implementation
        final YokeHttpServerRequest req = (YokeHttpServerRequest) request;

        String authorization = req.headers().get("authorization");

        if (authorization == null) {
            req.response().putHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
            req.response().setStatusCode(401);
            next.handle("No authorization token");
        } else {
            String[] parts = authorization.split(" ");
            String scheme = parts[0];
            String[] credentials = new String(DatatypeConverter.parseBase64Binary(parts[1])).split(":");
            final String user = credentials[0];
            final String pass = credentials[1];

            if (!"Basic".equals(scheme)) {
                next.handle(400);
            } else {
                authHandler.handle(user, pass, new Handler<Boolean>() {
                    @Override
                    public void handle(Boolean valid) {
                        if (valid) {
                            req.put("user", user);
                            next.handle(null);
                        } else {
                            req.response().putHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
                            req.response().setStatusCode(401);
                            next.handle("No authorization token");
                        }
                    }
                });
            }
        }
    }
}
