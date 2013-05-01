package com.jetdrone.vertx.yoke.middleware

import com.jetdrone.vertx.yoke.Middleware
import groovy.transform.CompileStatic
import org.vertx.java.core.Handler

import java.util.regex.Pattern

@CompileStatic public class GRouter extends Middleware {

    private final Router jRouter = new Router()

    @Override
    public void handle(YokeHttpServerRequest request, Handler<Object> next) {
        jRouter.handle(request, next)
    }

    private static Middleware wrapClosure(Closure closure) {
        return new Middleware() {
            @Override
            void handle(YokeHttpServerRequest request, Handler<Object> next) {
                int params = closure.maximumNumberOfParameters
                if (params == 1) {
                    closure.call(request);
                } else if (params == 2) {
                    closure.call(request, next);
                } else {
                    throw new RuntimeException('Cannot infer the closure signature, should be: request [, next]')
                }
            }
        }
    }

    /**
     * Specify a middleware that will be called for a matching HTTP GET
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter get(String pattern, Closure handler) {
        jRouter.get(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PUT
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter put(String pattern, Closure handler) {
        jRouter.put(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP POST
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter post(String pattern, Closure handler) {
        jRouter.post(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP DELETE
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter delete(String pattern, Closure handler) {
        jRouter.delete(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP OPTIONS
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter options(String pattern, Closure handler) {
        jRouter.options(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP HEAD
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter head(String pattern, Closure handler) {
        jRouter.head(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP TRACE
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter trace(String pattern, Closure handler) {
        jRouter.trace(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP CONNECT
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter connect(String pattern, Closure handler) {
        jRouter.trace(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PATCH
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter patch(String pattern, Closure handler) {
        jRouter.trace(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for all HTTP methods
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter all(String pattern, Closure handler) {
        jRouter.all(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP GET
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter get(Pattern pattern, Closure handler) {
        jRouter.get(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PUT
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter put(Pattern pattern, Closure handler) {
        jRouter.put(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP POST
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter post(Pattern pattern, Closure handler) {
        jRouter.post(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP DELETE
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter delete(Pattern pattern, Closure handler) {
        jRouter.delete(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP OPTIONS
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter options(Pattern pattern, Closure handler) {
        jRouter.options(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP HEAD
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter head(Pattern pattern, Closure handler) {
        jRouter.head(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP TRACE
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter trace(Pattern pattern, Closure handler) {
        jRouter.trace(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP CONNECT
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter connect(Pattern pattern, Closure handler) {
        jRouter.trace(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PATCH
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter patch(Pattern pattern, Closure handler) {
        jRouter.trace(pattern, wrapClosure(handler))
        this
    }

    /**
     * Specify a middleware that will be called for all HTTP methods
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public GRouter all(Pattern pattern, Closure handler) {
        jRouter.all(pattern, wrapClosure(handler))
        this
    }
}
