// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke
package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

// # Middleware
//
// Abstract class that needs to be implemented when creating a new middleware piece.
// The class provides access to the Vertx object and by default is not marked as a error handler middleware.
//
// If there is a need to create a new error handler middleware the isErrorHandler method should be overridden to
// return true.
//
// The current list of Middleware is:
// [BasicAuth](middleware/BasicAuth.html),
// [BodyParser](middleware/BodyParser.html),
// [BridgeSecureHandler](middleware/BridgeSecureHandler.html),
// [Compress](middleware/Compress.html),
// [CookieParser](middleware/CookieParser.html),
// [Csrf](middleware/Csrf.html),
// [ErrorHandler](middleware/ErrorHandler.html),
// [Favicon](middleware/Favicon.html),
// [Limit](middleware/Limit.html),
// [Logger](middleware/Logger.html),
// [MethodOverride](middleware/MethodOverride.html),
// [RequestProxy](middleware/RequestProxy.html),
// [ResponseTime](middleware/ResponseTime.html),
// [Router](middleware/Router.html),
// [Session](middleware/Session.html),
// [Static](middleware/Static.html),
// [Timeout](middleware/Timeout.html),
// [Vhost](middleware/Vhost.html).
//
// Using the extras project you get the following extra Middleware:
// [JsonRestRouter].
public abstract class Middleware {

    // Local Vert.x instance for usage within the middleware. This is useful to use all asynchronous features of it.
    //
    // @property vertx
    // @protected
    protected Vertx vertx;

    // The configured mount point for this middleware.
    //
    // @property mount
    // @protected
    protected String mount;

    // Initializes the middleware. This methos is called from Yoke once a middleware is added to the chain.
    //
    // @method init
    // @param {Vertx} vertx the local Vert.x instance.
    // @param {String} mount the configured mount path.
    // @return {Middleware}
    public Middleware init(Vertx vertx, String mount) {
        this.vertx = vertx;
        this.mount = mount;

        return this;
    }

    // When there is a need to identify a middleware to handle errors (error handler) this method should return true.
    //
    // @method isErrorHandler
    // @getter
    // @return {boolean} true is this middleware will handle errors.
    public boolean isErrorHandler() {
        return false;
    }

    // Handles a request that is inside the chain.
    //
    // @method handle
    // @asynchronous
    // @param {YokeRequest} request A YokeRequest which in practice is a extended HttpServerRequest
    // @param {Handler} next The callback to inform that the next middleware in the chain should be used. A value different from null represents an error and in that case the error handler middleware will be executed.
    //
    // @example
    //     // Example that always returns Hello
    //     class HelloMiddleware extends Middleware {
    //       public void handle(YokeRequest request, Handler<Object> next) {
    //         request.response.end("Hello");
    //       }
    //     }
    //
    // @example
    //     // Example that always raises an internal server error
    //     class HelloMiddleware extends Middleware {
    //       public void handle(YokeRequest request, Handler<Object> next) {
    //         next.handle("Something went wrong!");
    //       }
    //     }
    //
    // @example
    //     // Example that passes the control to the next middleware
    //     class HelloMiddleware extends Middleware {
    //       public void handle(YokeRequest request, Handler<Object> next) {
    //         // when the error is null, then the chain will execute
    //         // the next Middleware until the chain is complete,
    //         // when that happens a 404 error is returned since no
    //         // middleware was found that could handle the request.
    //         next.handle(null);
    //       }
    //     }
    public abstract void handle(final YokeRequest request, final Handler<Object> next);
}
