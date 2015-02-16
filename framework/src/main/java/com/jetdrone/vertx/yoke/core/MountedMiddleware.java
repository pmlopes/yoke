package com.jetdrone.vertx.yoke.core;

import com.jetdrone.vertx.yoke.IMiddleware;
import org.jetbrains.annotations.NotNull;

/**
 * Mounted middleware represents a binding of a Middleware instance to a specific url path.
 */
public final class MountedMiddleware {
    public final String mount;
    public final IMiddleware middleware;
    public boolean enabled = true;

    /**
     * Constructs a new Mounted Middleware
     *
     * @param mount      Mount path
     * @param middleware Middleware to use on the path.
     */
    public MountedMiddleware(@NotNull String mount, @NotNull IMiddleware middleware) {
        this.mount = mount;
        this.middleware = middleware;
    }

}
