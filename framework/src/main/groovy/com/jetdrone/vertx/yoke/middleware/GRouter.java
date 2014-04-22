/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.annotations.*;
import groovy.lang.Closure;
import org.codehaus.groovy.ast.AnnotationNode;
import org.vertx.java.core.Handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

public class GRouter extends Middleware {

    private final Router jRouter = new Router();

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        jRouter.handle(request, next);
    }

    private static Middleware wrapClosure(final Closure closure) {
        final int params = closure.getMaximumNumberOfParameters();
        return new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
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

    public GRouter add(Closure... closures) {

        for (Closure c : closures) {



            for (AnnotationNode a : c.getMetaClass().getClassNode().getAnnotations()) {
                System.out.println(a);
            }
        }
//            for (final Method m : o.getClass().getMethods()) {
//
//                if (!Modifier.isPublic(m.getModifiers())) {
//                    continue;
//                }
//
//                Annotation[] annotations = m.getAnnotations();
//                // this method is not annotated
//                if (annotations == null) {
//                    continue;
//                }
//
//                Class[] paramTypes = m.getParameterTypes();
//                int type = 0;
//
//                if (paramTypes != null) {
//                    if (paramTypes.length == 1 && YokeRequest.class.isAssignableFrom(paramTypes[0])) {
//                        // single argument handler
//                        type = 1;
//                    }
//                    if (paramTypes.length == 2 && YokeRequest.class.isAssignableFrom(paramTypes[0]) && Handler.class.isAssignableFrom(paramTypes[1])) {
//                        // double argument handler
//                        type = 2;
//                    }
//                }
//
//                if (type == 0) {
//                    continue;
//                }
//
//                String[] produces = null;
//                String[] consumes = null;
//
//                // identify produces/consumes for content negotiation
//                for (Annotation a : annotations) {
//                    if (a instanceof Consumes) {
//                        consumes = ((Consumes) a).value();
//                    }
//                    if (a instanceof Produces) {
//                        produces = ((Produces) a).value();
//                    }
//                }
//
//                // if still null inspect class
//                if (consumes == null) {
//                    Annotation c = o.getClass().getAnnotation(Consumes.class);
//                    if (c != null) {
//                        // top level consumes is present
//                        consumes = ((Consumes) c).value();
//                    }
//                }
//
//                if (produces == null) {
//                    Annotation p = o.getClass().getAnnotation(Produces.class);
//                    if (p != null) {
//                        // top level consumes is present
//                        produces = ((Produces) p).value();
//                    }
//                }
//
//                for (Annotation a : annotations) {
//                    if (a instanceof GET) {
//                        router.get(((GET) a).value(), wrap(o, m, type == 1, consumes, produces));
//                    }
//                    if (a instanceof PUT) {
//                        router.put(((PUT) a).value(), wrap(o, m, type == 1, consumes, produces));
//                    }
//                    if (a instanceof POST) {
//                        router.post(((POST) a).value(), wrap(o, m, type == 1, consumes, produces));
//                    }
//                    if (a instanceof DELETE) {
//                        router.delete(((DELETE) a).value(), wrap(o, m, type == 1, consumes, produces));
//                    }
//                    if (a instanceof OPTIONS) {
//                        router.options(((OPTIONS) a).value(), wrap(o, m, type == 1, consumes, produces));
//                    }
//                    if (a instanceof HEAD) {
//                        router.head(((HEAD) a).value(), wrap(o, m, type == 1, consumes, produces));
//                    }
//                    if (a instanceof TRACE) {
//                        router.trace(((TRACE) a).value(), wrap(o, m, type == 1, consumes, produces));
//                    }
//                    if (a instanceof PATCH) {
//                        router.patch(((PATCH) a).value(), wrap(o, m, type == 1, consumes, produces));
//                    }
//                    if (a instanceof CONNECT) {
//                        router.connect(((CONNECT) a).value(), wrap(o, m, type == 1, consumes, produces));
//                    }
//                    if (a instanceof ALL) {
//                        router.all(((ALL) a).value(), wrap(o, m, type == 1, consumes, produces));
//                    }
//
//                    // TODO: broken!!!
//                    if (a instanceof Param) {
//                        router.param(((Param) a).value(), wrap(o, m, type == 1, null, null));
//                    }
//                }
//            }
//        }

        return this;
    }

    public Router toJavaRouter() {
        return jRouter;
    }
}
