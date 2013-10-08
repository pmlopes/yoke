// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;

import javax.xml.bind.DatatypeConverter;

// # BasicAuth
//
// Enfore basic authentication by providing a AuthHandler.handler(user, pass), which must return true in order to gain
// access. Populates request.user. The final alternative is simply passing username / password strings.
public class BasicAuth extends Middleware {

    // ## AuthHandler
    // AuthHandler interface that needs to be implemented in order to validate usernames/passwords.
    public interface AuthHandler {
        // Handles a challenge authentication request and asynchronously returns true on success.
        //
        // @method handle
        // @asynchronous
        //
        // @param {String} username
        // @param {String} password
        // @param {Handler} result
        void handle(String username, String password, Handler<Boolean> result);
    }

    // Realm name for the application
    //
    // @property realm
    // @private
    private final String realm;

    // AuthHandler for validating this instance authentication requests.
    //
    // @property authHandler
    // @private
    private final AuthHandler authHandler;

    // Creates a new BasicAuth middleware with a master username / password and a given realm.
    //
    // @constructor
    // @param {String} username
    // @param {String} password
    // @param {String} realm
    //
    // @example
    //       Yoke yoke = new Yoke(...);
    //       yoke.use("/admin", new BasicAuth("admin", "s3cr37", "MyApp Auth Required"));
    public BasicAuth(final String username, final String password, String realm) {
        this.realm = realm;
        authHandler = new AuthHandler() {
            @Override
            public void handle(String _username, String _password, Handler<Boolean> result) {
                result.handle(username.equals(_username) && password.equals(_password));
            }
        };
    }

    // Creates a new BasicAuth middleware with a master username / password. By default the realm will be `Authentication required`.
    //
    // @constructor
    // @param {String} username
    // @param {String} password
    //
    // @example
    //       Yoke yoke = new Yoke(...);
    //       yoke.use("/admin", new BasicAuth("admin", "s3cr37"));
    public BasicAuth(String username, String password) {
        this (username, password, "Authentication required");

    }

    // Creates a new BasicAuth middleware with a AuthHandler and a given realm.
    //
    // @constructor
    // @param {AuthHandler} authHandler
    // @param {String} realm
    //
    // @example
    //       Yoke yoke = new Yoke(...);
    //       yoke.use("/admin", new AuthHandler() {
    //           public void handle(String user, String password, Handler next) {
    //                // a better example would be fetching user from a DB
    //                if ("user".equals(user) && "pass".equals(password)) {
    //                     next.handle(true);
    //                } else {
    //                     next.handle(false);
    //                }
    //           }
    //       }, "My App Auth");
    public BasicAuth(AuthHandler authHandler, String realm) {
        this.realm = realm;
        this.authHandler = authHandler;
    }

    // Creates a new BasicAuth middleware with a AuthHandler.
    //
    // @constructor
    // @param {AuthHandler} authHandler
    //
    // @example
    //       Yoke yoke = new Yoke(...);
    //       yoke.use("/admin", new AuthHandler() {
    //           public void handle(String user, String password, Handler next) {
    //                // a better example would be fetching user from a DB
    //                if ("user".equals(user) && "pass".equals(password)) {
    //                     next.handle(true);
    //                } else {
    //                     next.handle(false);
    //                }
    //           }
    //       });
    public BasicAuth(AuthHandler authHandler) {
        this(authHandler, "Authentication required");
    }

    // Handle all forbidden errors, in this case we need to add a special header to the response
    //
    // @method handle401
    // @private
    // @param {YokeRequest} request
    // @param {Handler} next
    private void handle401(final YokeRequest request, final Handler<Object> next) {
        YokeResponse response = request.response();
        response.putHeader("WWW-Authenticate", "Basic realm=\"" + getRealm(request) + "\"");
        response.setStatusCode(401);
        next.handle("No authorization token");
    }

    @Override
    public void handle(final YokeRequest request, final Handler<Object> next) {
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
                String[] credentials = new String(DatatypeConverter.parseBase64Binary(parts[1])).split(":");
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
                authHandler.handle(user, pass, new Handler<Boolean>() {
                    @Override
                    public void handle(Boolean valid) {
                        if (valid) {
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

    // Get the realm for this instance
    //
    // The usecase is a multitenant app where I want different realms for paths like /foo/homepage and /bar/homepage.
    //
    // @method getRealm
    // @param {YokeRequest} request http request
    // @return {String} realm name
    public String getRealm(YokeRequest request) {
        return realm;
    }
}
