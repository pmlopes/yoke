package com.jetdrone.vertx.yoke.core;

import com.jetdrone.vertx.yoke.Middleware;
import org.jetbrains.annotations.NotNull;

/**
 * Mounted middleware represents a binding of a Middleware instance to a specific url path.
 */
public class MountedMiddleware {
    public final String mount;
    public final Middleware middleware;

    /**
     * Constructs a new Mounted Middleware
     *
     * @param mount      Mount path
     * @param middleware Middleware to use on the path.
     */
    public MountedMiddleware(@NotNull String mount, @NotNull Middleware middleware) {
        this.mount = mount;
        this.middleware = middleware;
    }

}
