/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.core.Context;
import com.jetdrone.vertx.yoke.core.RequestWrapper;
import com.jetdrone.vertx.yoke.middleware.GYokeRequest;
import com.jetdrone.vertx.yoke.middleware.GYokeResponse;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.store.SessionStore;
import groovy.lang.Closure;
import org.vertx.groovy.platform.Container;
import org.vertx.groovy.platform.Verticle;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import org.vertx.groovy.core.Vertx;
import org.vertx.groovy.core.http.HttpServer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Yoke is a chain executor of middleware for Vert.x 2.x.
 * The goal of this library is not to provide a web application framework but
 * the backbone that helps the creation of web applications.
 *
 * Yoke works in a similar way to Connect middleware. Users start by declaring
 * which middleware components want to use and then start an http server either
 * managed by Yoke or provided by the user (say when you need https).
 *
 * Yoke has no extra dependencies than Vert.x itself so it is self contained.
 */
public class GYoke {

    private final Yoke jYoke;
    private final org.vertx.java.core.Vertx vertx;
    private final Container container;

    /**
     * Creates a Yoke instance.
     *
     * This constructor should be called from a verticle and pass a valid Vertx instance. This instance will be shared
     * with all registered middleware. The reason behind this is to allow middleware to use Vertx features such as file
     * system and timers.
     *
     * <pre>
     * public class MyVerticle extends Verticle {
     *   public void start() {
     *     def yoke = new Yoke(this)
     *     ...
     *   }
     * }
     * </pre>
     * @param verticle the main verticle
     */
    public GYoke(Verticle verticle) {
        this(verticle.getVertx(), verticle.getContainer());
    }

    /**
     * Creates a Yoke instance.
     * This constructor should be called from a verticle and pass a valid Vertx
     * instance. This instance will be shared with all registered middleware.
     * The reason behind this is to allow middleware to use Vertx features such
     * as file system and timers.
     *
     * @param vertx The Vertx instance
     */
    public GYoke(Vertx vertx, Container container) {
        this.vertx = vertx.toJavaVertx();
        this.container = container;

        jYoke = new Yoke(this.vertx, null, new RequestWrapper() {
            @Override
            public YokeRequest wrap(HttpServerRequest request, boolean secure, Map<String, Engine> engines) {
                // the context map is shared with all middlewares
                final Map<String, Object> context = new Context(jYoke.defaultContext);
                GYokeResponse response = new GYokeResponse(request.response(), context, engines);
                return new GYokeRequest(request, response, secure, context, jYoke.store);
            }
        });
    }

    public GYoke store(SessionStore store) {
        jYoke.store(store);
        return this;
    }

    /**
     * Adds a Middleware to the chain. If the middleware is an Error Handler Middleware then it is
     * treated differently and only the last error handler is kept.
     *
     * You might want to add a middleware that is only supposed to run on a specific route (path prefix).
     * In this case if the request path does not match the prefix the middleware is skipped automatically.
     *
     * @param route The route prefix for the middleware
     * @param closure The closure add to the chain
     */
    public GYoke use(String route, final Closure closure) {
        final int params = closure.getMaximumNumberOfParameters();
        jYoke.use(route, new Middleware() {
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
        });
        return this;
    }

    /**
     * Adds a middleware to the chain with the prefix "/".
     * @see Yoke#use(String, Middleware...)
     * @param closure The closure add to the chain
     */
    public GYoke use(Closure closure) {
        return use("/", closure);
    }

    /**
     * Adds a Middleware to the chain. If the middleware is an Error Handler Middleware then it is
     * treated differently and only the last error handler is kept.
     *
     * You might want to add a middleware that is only supposed to run on a specific route (path prefix).
     * In this case if the request path does not match the prefix the middleware is skipped automatically.
     *
     * @param route The route prefix for the middleware
     * @param middleware The middleware add to the chain
     */
    public GYoke use(String route, Middleware... middleware) {
        jYoke.use(route, middleware);
        return this;
    }

    /**
     * Adds a middleware to the chain with the prefix "/".
     * @see Yoke#use(String, Middleware...)
     * @param middleware The middleware add to the chain
     */
    public GYoke use(Middleware... middleware) {
        return use("/", middleware);
    }

    /**
     * Adds a Render Engine to the library. Render Engines are Template engines you
     * might want to use to speed the development of your application. Once they are
     * registered you can use the method render in the YokeResponse to
     * render a template.
     *
     * @param extension The file extension for this template engine e.g.: .jsp
     * @param engine The implementation of the engine
     */
    public GYoke engine(String extension, Engine engine) {
        jYoke.engine(extension, engine);
        return this;
    }

    /**
     * When you need to share global properties with your requests you can add them
     * to Yoke and on every request they will be available as request.get(String)
     *
     * @param key unique identifier
     * @param value Any non null value, nulls are not saved
     */
    public GYoke set(String key, Object value) {
        jYoke.set(key, value);
        return this;
    }

    /**
     * Starts the server listening at a given port bind to all available interfaces.
     *
     * @param port the server TCP port
     * @return Yoke
     */
    public GYoke listen(int port) {
        return listen(port, "0.0.0.0");
    }

    /**
     * Starts the server listening at a given port and given address.
     *
     * @param port the server TCP port
     * @return Yoke
     */
    public GYoke listen(int port, String address) {
        // create the server
        org.vertx.java.core.http.HttpServer server = vertx.createHttpServer();
        // setup the request handler
        jYoke.listen(server);
        // start listening
        server.listen(port, address);
        return this;
    }

    /**
     * Starts listening at a already created server.
     * @return Yoke
     */
    public GYoke listen(HttpServer gserver) {
        org.vertx.java.core.http.HttpServer server = gserver.toJavaServer();
        jYoke.listen(server);
        return this;
    }

    /** Deploys required middleware
     *
     * @param config configuration to use
     * @param handler Closure tho allow asynchronous result handling
     */
    @SuppressWarnings("unchecked")
    public GYoke deploy(Object config, Closure handler) {
        if (config instanceof List) {
            for (Object o : (List) config) {
                Map mod = (Map) o;

                String module = (String) mod.get("module");
                String verticle = (String) mod.get("verticle");
                Integer instances = (Integer) mod.get("instances");
                Map modConfig = (Map) mod.get("config");
                Boolean worker = (Boolean) mod.get("worker");
                Boolean multiThreaded = (Boolean) mod.get("multiThreaded");

                instances = instances == null ? 1 : instances;
                modConfig = modConfig == null ? Collections.emptyMap() : modConfig;
                worker = worker == null ? false : worker;
                multiThreaded = multiThreaded == null ? false : multiThreaded;

                if (module != null) {
                    deploy(module, true, false, false, instances, modConfig, handler);
                } else {
                    deploy(verticle, false, worker, multiThreaded, instances, modConfig, handler);
                }
            }
        } else {
            Map mod = (Map) config;

            String module = (String) mod.get("module");
            String verticle = (String) mod.get("verticle");
            Integer instances = (Integer) mod.get("instances");
            Map modConfig = (Map) mod.get("config");
            Boolean worker = (Boolean) mod.get("worker");
            Boolean multiThreaded = (Boolean) mod.get("multiThreaded");

            instances = instances == null ? 1 : instances;
            modConfig = modConfig == null ? Collections.emptyMap() : modConfig;
            worker = worker == null ? false : worker;
            multiThreaded = multiThreaded == null ? false : multiThreaded;

            if (module != null) {
                deploy(module, true, false, false, instances, modConfig, handler);
            } else {
                deploy(verticle, false, worker, multiThreaded, instances, modConfig, handler);
            }
        }

        return this;
    }

    private void deploy(String name, boolean module, boolean worker, boolean multiThreaded, int instances, Map<String, Object> config, Closure handler) {
        if (module) {
            if (handler != null) {
                container.deployModule(name, config, instances, handler);
            } else {
                container.deployModule(name, config, instances);
            }
        } else {
            if (worker) {
                if (handler != null) {
                    container.deployWorkerVerticle(name, config, instances, multiThreaded, handler);
                } else {
                    container.deployWorkerVerticle(name, config, instances, multiThreaded);
                }
            } else {
                if (handler != null) {
                    container.deployVerticle(name, config, instances, handler);
                } else {
                    container.deployVerticle(name, config, instances);
                }
            }
        }
    }

    public Yoke toJavaYoke() {
        return jYoke;
    }
}
