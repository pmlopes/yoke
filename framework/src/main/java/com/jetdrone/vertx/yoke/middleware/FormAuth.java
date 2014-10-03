/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.AbstractMiddleware;
import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.store.json.SessionObject;
import com.jetdrone.vertx.yoke.util.Utils;

public class FormAuth extends AbstractMiddleware {

    private final AuthHandler authHandler;

    private String loginURI;
    private String logoutURI;

    private String loginTemplate;
    private String userLoginTemplate;

    private final boolean forceSSL;

    public FormAuth(@NotNull final AuthHandler authHandler) {
        this(false, authHandler);
    }

    public FormAuth(final boolean forceSSL, @NotNull final AuthHandler authHandler) {
        this(forceSSL, "/login", "/logout", null, authHandler);
    }

    public FormAuth(final boolean forceSSL, @NotNull final String loginURI, @NotNull final String logoutURI, final String loginTemplate, @NotNull final AuthHandler authHandler) {
        this.authHandler = authHandler;
        this.loginURI = loginURI;
        this.logoutURI = logoutURI;
        this.userLoginTemplate = loginTemplate;
        this.forceSSL = forceSSL;

        if (this.userLoginTemplate == null) {
            this.loginTemplate = Utils.readResourceToBuffer(getClass(), "login.html").toString();
        }
    }

    @Override
    public Middleware init(@NotNull final Yoke yoke, @NotNull final String mount) {
        super.init(yoke, mount);
        // trim the initial slash
        String correctedMount = mount;
        if (mount.endsWith("/")) {
            correctedMount = correctedMount.substring(0, correctedMount.length() - 1);
        }
        loginURI = correctedMount + loginURI;
        logoutURI = correctedMount + logoutURI;
        return this;
    }

    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
        if (request.path().equals(loginURI)) {
            if ("GET".equals(request.method())) {
                if (loginTemplate != null) {
                    // render internal login
                    request.response().setContentType("text/html");
                    request.response().end(
                            loginTemplate.replace("{title}", (String) request.get("title"))
                                    .replace("{action}", loginURI + "?redirect_url=" + Utils.encodeURIComponent(request.getParameter("redirect_url", "/")))
                                    .replace("{message}", ""));
                } else {
                    // render login
                    request.response().render(userLoginTemplate, next);
                }
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
                            JsonObject session = request.createSession();
                            session.putString("user", request.getFormParameter("username"));

                            // get the redirect_url parameter
                            String redirect = request.getParameter("redirect_url", "/");
                            request.response().redirect(Utils.decodeURIComponent(redirect));
                        } else {
                            if (loginTemplate != null) {
                                // render internal login
                                request.response().setContentType("text/html");
                                request.response().setStatusCode(401);
                                request.response().end(
                                        loginTemplate.replace("{title}", (String) request.get("title"))
                                                .replace("{action}", loginURI + "?redirect_url=" + Utils.encodeURIComponent(request.getParameter("redirect_url", "/")))
                                                .replace("{message}", "Invalid username and/or password, please try again."));
                            } else {
                                next.handle(401);
                            }
                        }
                    }
                });

                return;
            }
        }

        if (request.path().equals(logoutURI)) {
            if ("GET".equals(request.method())) {
                // remove session from storage
                request.destroySession();
                // get the redirect_url parameter
                String redirect = request.getParameter("redirect_url", "/");
                request.response().redirect(Utils.decodeURIComponent(redirect));
                return;
            }
        }

        // all others continue
        next.handle(null);
    }

    public final Middleware RequiredAuth = new Middleware() {
        @Override
        public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
        	SessionObject session = request.get("session");

            if (session != null) {
                if (session.getString("id") != null) {
                    next.handle(null);
                    return;
                }
            }

            String redirect = request.getParameter("redirect_url", Utils.encodeURIComponent(request.uri()));
            request.response().redirect(loginURI + "?redirect_url=" + Utils.decodeURIComponent(redirect));
        }
    };
}
