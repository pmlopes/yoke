/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetdrone.vertx.yoke

import com.jetdrone.vertx.yoke.middleware.YokeHttpServerRequest
import groovy.transform.CompileStatic
import org.vertx.java.core.Handler
import org.vertx.java.core.Vertx
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.core.http.HttpServer

import com.jetdrone.vertx.yoke.middleware.GYokeHttpServerResponse

import org.vertx.groovy.core.Vertx as GVertx
import org.vertx.groovy.core.http.HttpServer as GHttpServer

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
@CompileStatic public class GYoke {

    private final Yoke jYoke;
    private final Vertx vertx;

    /**
     * Creates a Yoke instance.
     * This constructor should be called from a verticle and pass a valid Vertx
     * instance. This instance will be shared with all registered middleware.
     * The reason behind this is to allow middleware to use Vertx features such
     * as file system and timers.
     *
     * @param vertx The Vertx instance
     */
    public GYoke(GVertx vertx) {
        this.vertx = vertx.toJavaVertx();
        jYoke = new Yoke(this.vertx, new HttpServerRequestWrapper() {
            @Override
            YokeHttpServerRequest wrap(HttpServerRequest request, Map<String, Object> context, Map<String, Engine> engines) {
                GYokeHttpServerResponse response = new GYokeHttpServerResponse(request.response(), context, engines);
                return new YokeHttpServerRequest(request, response, context);
            }
        });
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
    public GYoke chain(String route, Closure closure) {
        jYoke.use(route, new Middleware() {
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
        });
        this;
    }

    /**
     * Adds a middleware to the chain with the prefix "/".
     * @see Yoke#use(String, Middleware)
     * @param middleware The middleware add to the chain
     */
    public GYoke chain(Closure closure) {
        return chain("/", closure);
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
    public GYoke chain(String route, Middleware middleware) {
        jYoke.use(route, middleware);
        this;
    }

    /**
     * Adds a middleware to the chain with the prefix "/".
     * @see Yoke#use(String, Middleware)
     * @param middleware The middleware add to the chain
     */
    public GYoke chain(Middleware middleware) {
        return chain("/", middleware);
    }

    /**
     * Adds a Render Engine to the library. Render Engines are Template engines you
     * might want to use to speed the development of your application. Once they are
     * registered you can use the method render in the YokeHttpServerResponse to
     * render a template.
     *
     * @param extension The file extension for this template engine e.g.: .jsp
     * @param engine The implementation of the engine
     */
    public GYoke engine(String extension, Engine engine) {
        jYoke.engine(extension, engine);
        this;
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
        this;
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
        HttpServer server = vertx.createHttpServer();
        // setup the request handler
        jYoke.listen(server);
        // start listening
        server.listen(port, address);
        this;
    }

    /**
     * Starts listening at a already created server.
     * @return Yoke
     */
    public GYoke listen(GHttpServer gserver) {
        HttpServer server = gserver.toJavaServer();
        jYoke.listen(server);
        this;
    }
}
