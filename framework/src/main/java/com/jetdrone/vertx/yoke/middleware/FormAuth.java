package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class FormAuth extends Middleware {

    // This is an example only, use a proper persistent storage
    final ConcurrentMap<String, String> storage = vertx.sharedData().getMap("session.storage.form.auth");

    private final AuthHandler authHandler;

    private String loginURI;
    private String logoutURI;

    private final boolean forceSSL;

    public FormAuth() {
        // allow configure paths /login, /logout
        authHandler = null;
        loginURI = "/login";
        logoutURI = "/logout";
        forceSSL = false;
    }

    @Override
    public Middleware init(final Vertx vertx, final Logger logger, final String mount) {
        super.init(vertx, logger, mount);
        // trim the initial slash
        loginURI = mount.substring(1) + loginURI;
        logoutURI = mount.substring(1) + logoutURI;
        return this;
    }

    @Override
    public void handle(final YokeRequest request, final Handler<Object> next) {
        if (request.path().equals(loginURI)) {
            if ("GET".equals(request.method())) {
                // render login
                request.response().render("login", next);
                return;
            }

            if ("POST".equals(request.method())) {
                if (forceSSL && !request.isSecure()) {
                    // SSL is required but the post is insecure
                    next.handle(400);
                    return;
                }

                authHandler.handle(request.getFormParameter("username"), request.getFormParameter("password"), new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject user) {
                        if (user != null) {
                            // generate a session Id
                            String sid = UUID.randomUUID().toString();

                            request.setSessionId(sid);
                            // TODO: save it and associate to the user id

                            // get the redirect_url parameter
                            String redirect = request.getParameter("redirect_url", "/");

                            request.response().redirect(redirect);
                        } else {
                            // TODO: how to handle errors in a nicer way?
                            next.handle(401);
                            return;
                        }
                    }
                });
            }
        }

        if (request.path().equals(logoutURI)) {
            if ("GET".equals(request.method())) {
                // remove session from storage
                String sid = request.getSessionId();
                storage.remove(sid == null ? "" : sid);
                // destroy session
                request.setSessionId(null);
                request.response().end();
                return;
            }
        }

        // all others continue
        next.handle(null);
    }

    public static class RequiredAuth extends Middleware {
        @Override
        public void handle(YokeRequest request, Handler<Object> next) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static class UserExists extends Middleware {
        @Override
        public void handle(YokeRequest request, Handler<Object> next) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
