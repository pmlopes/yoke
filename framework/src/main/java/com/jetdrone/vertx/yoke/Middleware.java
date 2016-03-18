/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;

/**
 * # Middleware
 *
 * Abstract class that needs to be implemented when creating a new middleware piece.
 * The class provides access to the Vertx object and by default is not marked as a error handler middleware.
 *
 * If there is a need to create a new error handler middleware the isErrorHandler method should be overridden to
 * return true.
 *
 * The current list of Middleware is:
 * * [BasicAuth](middleware/BasicAuth.html),
 * * [BodyParser](middleware/BodyParser.html),
 * * [BridgeSecureHandler](middleware/BridgeSecureHandler.html),
 * * [Compress](middleware/Compress.html),
 * * [CookieParser](middleware/CookieParser.html),
 * * [Csrf](middleware/Csrf.html),
 * * [ErrorHandler](middleware/ErrorHandler.html),
 * * [Favicon](middleware/Favicon.html),
 * * [Limit](middleware/Limit.html),
 * * [Logger](middleware/Logger.html),
 * * [MethodOverride](middleware/MethodOverride.html),
 * * [RequestProxy](middleware/RequestProxy.html),
 * * [ResponseTime](middleware/ResponseTime.html),
 * * [Router](middleware/Router.html),
 * * [Session](middleware/Session.html),
 * * [Static](middleware/Static.html),
 * * [Timeout](middleware/Timeout.html),
 * * [Vhost](middleware/Vhost.html).
 *
 * Using the extras project you get the following extra Middleware:
 * * [JsonRestRouter].
 */
public abstract class Middleware implements IMiddleware {

    /**
     * Local Yoke instance for usage within the middleware. This is useful to use all asynchronous features of it.
     */
    protected Yoke yoke;

    /**
     * The configured mount point for this middleware.
     */
    protected String mount;

    /**
     * Internal flag to ensure that middleware is initialized only once
     */
    private boolean initialized = false;

    /**
     * Initializes the middleware. This methos is called from Yoke once a middleware is added to the chain.
     *
     * @param yoke the local Yoke instance.
     * @param mount the configured mount path.
     * @return self
     */
    public Middleware init(@NotNull final Yoke yoke, @NotNull final String mount) {

        if (initialized) {
            throw new RuntimeException("Already Initialized!");
        }

        this.yoke = yoke;
        this.mount = mount;
        this.initialized = true;

        return this;
    }

    /**
     * Read only check if this middleware instance has been initialized.
     * @return true if the init method has been called
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Get access to the event bus
     * @return eventBus
     */
    public EventBus eventBus() {
        return vertx().eventBus();
    }

    /**
     * Get access to the FileSystem object from Vert.x
     * @return fileSystem
     */
    public FileSystem fileSystem() {
        return vertx().fileSystem();
    }

    /**
     * Get access to the security object from Yoke
     * @return security
     */
    public YokeSecurity security() {
        return yoke.security();
    }

    /**
     * Get access to Vert.x object
     * @return vertx
     */
    public Vertx vertx() {
        return yoke.vertx();
    }

    /**
     * When there is a need to identify a middleware to handle errors (error handler) this method should return true.
     *
     * @return true is this middleware will handle errors.
     */
    public boolean isErrorHandler() {
        return false;
    }
}
