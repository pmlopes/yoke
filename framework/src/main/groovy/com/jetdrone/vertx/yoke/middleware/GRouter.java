/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.AbstractMiddleware;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import groovy.lang.Closure;
import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.Handler;

import java.util.List;
import java.util.regex.Pattern;

public class GRouter extends AbstractMiddleware {

    private final Router jRouter = new Router();

    @Override
    public Middleware init(@NotNull final Yoke yoke, @NotNull final String mount) {
        jRouter.init(yoke, mount);
        return this;
    }

    @Override
    public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        jRouter.handle(request, next);
    }

    private static Middleware wrapClosure(final Closure closure) {
        final int params = closure.getMaximumNumberOfParameters();
        return new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                if (params == 1) {
                    closure.call(request);
                } else if (params == 2) {
                    closure.call(request, next);
                } else {
                    throw new RuntimeException("Cannot infer the closure signature, should be: request [, next]");
                }
            }
        };
    }

    /**
     * Specify a middleware that will be called for a matching HTTP GET
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter get(String pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.get(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PUT
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter put(String pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.put(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP POST
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter post(String pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.post(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP DELETE
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter delete(String pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.delete(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP OPTIONS
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter options(String pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.options(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP HEAD
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter head(String pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.head(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP TRACE
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter trace(String pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.trace(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP CONNECT
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter connect(String pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.trace(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PATCH
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter patch(String pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.trace(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for all HTTP methods
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter all(String pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.all(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP GET
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter get(Pattern pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.get(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PUT
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter put(Pattern pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.put(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP POST
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter post(Pattern pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.post(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP DELETE
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter delete(Pattern pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.delete(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP OPTIONS
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter options(Pattern pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.options(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP HEAD
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter head(Pattern pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.head(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP TRACE
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter trace(Pattern pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.trace(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP CONNECT
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter connect(Pattern pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.trace(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PATCH
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter patch(Pattern pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.trace(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for all HTTP methods
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter all(Pattern pattern, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.all(pattern, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for all HTTP methods
     * @param param The simple pattern
     * @param handlers The middleware to call
     */
    public GRouter param(String param, Closure... handlers) {
        for (Closure handler : handlers) {
            jRouter.param(param, wrapClosure(handler));
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for all HTTP methods
     * @param param The simple pattern
     * @param pattern The RegExp to validate the param
     */
    public GRouter param(String param, Pattern pattern) {
        jRouter.param(param, pattern);
        return this;
    }

    public Router toJavaRouter() {
        return jRouter;
    }

    public static GRouter from(Object... objs) {
        final GRouter router = new GRouter();
        from(router, objs);

        return router;
    }

    public static GRouter from(final GRouter router, Object... objs) {
        Router.from(router.jRouter, objs);
        return router;
    }

    public static GRouter from(List<Object> objs) {
        final GRouter router = new GRouter();
        from(router, objs.toArray());

        return router;
    }

    public static GRouter from(final GRouter router, List<Object> objs) {
        Router.from(router.jRouter, objs.toArray());
        return router;
    }
}
