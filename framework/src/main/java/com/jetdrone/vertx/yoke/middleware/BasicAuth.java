/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.Base64;

/**
 * # BasicAuth
 *
 * Enfore basic authentication by providing a AuthHandler.handler(user, pass), which must return true in order to gain
 * access. Populates request.user. The final alternative is simply passing username / password strings.
 */
public class BasicAuth extends Middleware {

    /**
     * Realm name for the application
     */
    private final String realm;

    /**
     * AuthHandler for validating this instance authentication requests.
     */
    private final AuthHandler authHandler;

    /**
     * Creates a new BasicAuth middleware with a master username / password and a given realm.
     * <pre>
     *   Yoke yoke = new Yoke(...);
     *     yoke.use("/admin", new BasicAuth("admin", "s3cr37",
     *         "MyApp Auth Required"));
     * </pre>
     *
     * @param username the security principal user name
     * @param password the security principal password
     * @param realm the security realm
     */
    public BasicAuth(@NotNull final String username, @NotNull final String password, @NotNull String realm) {
        this.realm = realm;
        authHandler = new AuthHandler() {
            @Override
            public void handle(String _username, String _password, Handler<JsonObject> result) {
                boolean success = username.equals(_username) && password.equals(_password);
                if (success) {
                    result.handle(new JsonObject().put("username", _username));
                } else {
                    result.handle(null);
                }
            }
        };
    }

    /**
     * Creates a new BasicAuth middleware with a master username / password. By default the realm will be `Authentication required`.
     *
     * <pre>
     *       Yoke yoke = new Yoke(...);
     *       yoke.use("/admin", new BasicAuth("admin", "s3cr37"));
     * </pre>
     *
     * @param username the security principal user name
     * @param password the security principal password
     */
    public BasicAuth(@NotNull String username, @NotNull String password) {
        this (username, password, "Authentication required");

    }

    /**
     * Creates a new BasicAuth middleware with a AuthHandler and a given realm.
     *
     * <pre>
     *       Yoke yoke = new Yoke(...);
     *       yoke.use("/admin", new AuthHandler() {
     *         public void handle(String user, String password, Handler next) {
     *           // a better example would be fetching user from a DB
     *           if ("user".equals(user) &amp;&amp; "pass".equals(password)) {
     *             next.handle(true);
     *           } else {
     *             next.handle(false);
     *           }
     *         }
     *       }, "My App Auth");
     * </pre>
     *
     * @param authHandler the authentication handler
     * @param realm the security realm
     */
    public BasicAuth(@NotNull String realm, @NotNull AuthHandler authHandler) {
        this.realm = realm;
        this.authHandler = authHandler;
    }

    /**
     * Creates a new BasicAuth middleware with a AuthHandler.
     *
     * <pre>
     *       Yoke yoke = new Yoke(...);
     *       yoke.use("/admin", new AuthHandler() {
     *         public void handle(String user, String password, Handler next) {
     *           // a better example would be fetching user from a DB
     *           if ("user".equals(user) &amp;&amp; "pass".equals(password)) {
     *             next.handle(true);
     *           } else {
     *             next.handle(false);
     *           }
     *         }
     *       });
     * </pre>

     * @param authHandler the authentication handler
     */
    public BasicAuth(@NotNull AuthHandler authHandler) {
        this("Authentication required", authHandler);
    }

    /**
     * Handle all forbidden errors, in this case we need to add a special header to the response
     *
     * @param request yoke request
     * @param next middleware to be called next
     */
    private void handle401(final YokeRequest request, final Handler<Object> next) {
        YokeResponse response = request.response();
        response.putHeader("WWW-Authenticate", "Basic realm=\"" + getRealm(request) + "\"");
        response.setStatusCode(401);
        next.handle("No authorization token");
    }

    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
        String authorization = request.getHeader("authorization");

        if (authorization == null) {
            handle401(request, next);
        } else {
            final String user;
            final String pass;
            final String scheme;

            try {
                String[] parts = authorization.split(" ");
                scheme = parts[0];
                String[] credentials = new String(Base64.getDecoder().decode(parts[1])).split(":");
                user = credentials[0];
                // when the header is: "user:"
                pass = credentials.length > 1 ? credentials[1] : null;
            } catch (ArrayIndexOutOfBoundsException e) {
				handle401(request, next);
				return;
			} catch (IllegalArgumentException | NullPointerException e) {
                // IllegalArgumentException includes PatternSyntaxException
                next.handle(e);
                return;
            }

            if (!"Basic".equals(scheme)) {
                next.handle(400);
            } else {
                authHandler.handle(user, pass, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject json) {
                        if (json != null) {
                            request.put("user", user);
                            next.handle(null);
                        } else {
                            handle401(request, next);
                        }
                    }
                });
            }
        }
    }

    /**
     * Get the realm for this instance
     *
     * The usecase is a multitenant app where I want different realms for paths like /foo/homepage and /bar/homepage.
     *
     * @param request http yoke request
     * @return realm name
     */
    public String getRealm(@NotNull YokeRequest request) {
        return realm;
    }
}
