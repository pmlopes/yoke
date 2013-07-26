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
package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import java.util.*;

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
public class Yoke implements RequestWrapper {

    private final Vertx vertx;
    private final Logger logger;
    // the request wrapper in use
    private final RequestWrapper requestWrapper;

    private final Map<String, Object> defaultContext = new HashMap<>();
    private final Map<String, Engine> engineMap = new HashMap<>();

    /**
     * Creates a Yoke instance.
     * This constructor should be called from a verticle and pass a valid Vertx
     * instance. This instance will be shared with all registered middleware.
     * The reason behind this is to allow middleware to use Vertx features such
     * as file system and timers.
     */
    public Yoke(Verticle verticle) {
        this(verticle.getVertx(), verticle.getContainer().logger(), null);
    }

    public Yoke(Vertx vertx, Logger logger) {
        this(vertx, logger, null);
    }

    public Yoke(Vertx vertx, Logger logger, RequestWrapper requestWrapper) {
        this.vertx = vertx;
        this.logger = logger;
        this.requestWrapper = requestWrapper == null ? this : requestWrapper;
        // defaults
        defaultContext.put("title", "Yoke");
        defaultContext.put("x-powered-by", true);
        defaultContext.put("trust-proxy", true);
    }

    private static class MountedMiddleware {
        final String mount;
        final Middleware middleware;

        MountedMiddleware(String mount, Middleware middleware) {
            this.mount = mount;
            this.middleware = middleware;
        }
    }

    private final List<MountedMiddleware> middlewareList = new ArrayList<>();
    private Middleware errorHandler;

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
    public Yoke use(String route, Middleware middleware) {
        if (middleware.isErrorHandler()) {
            errorHandler = middleware;
        } else {
            middlewareList.add(new MountedMiddleware(route, middleware));
        }

        // initialize the middleware
        middleware.init(vertx, logger);
        return this;
    }

    /**
     * Adds a middleware to the chain with the prefix "/".
     * @see Yoke#use(String, Middleware)
     * @param middleware The middleware add to the chain
     */
    public Yoke use(Middleware middleware) {
        return use("/", middleware);
    }

    /**
     * Adds a Handler to a route. The behaviour is similar to the middleware, however this
     * will be a terminal point in the execution chain. In this case any middleware added
     * after will not be executed. However you should care about the route which may lead
     * to skip this middleware.
     *
     * The idea to user a Handler is to keep the API familiar with the rest of the Vert.x
     * API.
     *
     * @see Yoke#use(String, Middleware)
     * @param route The route prefix for the middleware
     * @param handler The Handler to add
     */
    public Yoke use(String route, final Handler<YokeRequest> handler) {
        middlewareList.add(new MountedMiddleware(route, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        }));
        return this;
    }

    /**
     * Adds a Handler to a route.
     *
     * @see Yoke#use(String, Handler)
     * @param handler The Handler to add
     */
    public Yoke use(Handler<YokeRequest> handler) {
        return use("/", handler);
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
    public Yoke engine(String extension, Engine engine) {
        engine.setVertx(vertx);
        engineMap.put(extension, engine);
        return this;
    }

    /**
     * When you need to share global properties with your requests you can add them
     * to Yoke and on every request they will be available as request.get(String)
     *
     * @param key unique identifier
     * @param value Any non null value, nulls are not saved
     */
    public Yoke set(String key, Object value) {
        if (value == null) {
            defaultContext.remove(key);
        } else {
            defaultContext.put(key, value);
        }

        return this;
    }

    /**
     * Starts the server listening at a given port bind to all available interfaces.
     *
     * @param port the server TCP port
     * @return Yoke
     */
    public Yoke listen(int port) {
        return listen(port, "0.0.0.0");
    }

    /**
     * Starts the server listening at a given port and given address.
     *
     * @param port the server TCP port
     * @return Yoke
     */
    public Yoke listen(int port, String address) {
        HttpServer server = vertx.createHttpServer();

        listen(server);

        server.listen(port, address);
        return this;
    }

    /**
     * Default implementation of the request wrapper
     */
    @Override
    public YokeRequest wrap(HttpServerRequest request, boolean secure, Map<String, Object> context, Map<String, Engine> engines) {
        YokeResponse response = new YokeResponse(request.response(), context, engines);
        return new YokeRequest(request, response, secure, context);
    }

    /**
     * Starts listening at a already created server.
     * @return Yoke
     */
    public Yoke listen(final HttpServer server) {
        // is this server HTTPS?
        final boolean secure = server.isSSL();

        server.requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest req) {
                // the context map is shared with all middlewares
                final Map<String, Object> context = new HashMap<>(defaultContext);
                final YokeRequest request = requestWrapper.wrap(req, secure, context, engineMap);

                // add x-powered-by header is enabled
                Object poweredBy = context.get("x-powered-by");
                if (poweredBy != null && (Boolean) poweredBy) {
                    request.response().putHeader("x-powered-by", "yoke");
                }

                new Handler<Object>() {
                    int currentMiddleware = -1;
                    @Override
                    public void handle(Object error) {
                        if (error == null) {
                            currentMiddleware++;
                            if (currentMiddleware < middlewareList.size()) {
                                MountedMiddleware mountedMiddleware = middlewareList.get(currentMiddleware);

                                if (request.path().startsWith(mountedMiddleware.mount)) {
                                    Middleware middlewareItem = mountedMiddleware.middleware;
                                    middlewareItem.handle(request, this);
                                } else {
                                    // the middleware was not mounted on this uri, skip to the next entry
                                    handle(null);
                                }
                            } else {
                                HttpServerResponse response = request.response();
                                // reached the end and no handler was able to answer the request
                                response.setStatusCode(404);
                                response.setStatusMessage(HttpResponseStatus.valueOf(404).reasonPhrase());
                                if (errorHandler != null) {
                                    errorHandler.handle(request, null);
                                } else {
                                    response.end(HttpResponseStatus.valueOf(404).reasonPhrase());
                                }
                            }
                        } else {
                            request.put("error", error);
                            if (errorHandler != null) {
                                errorHandler.handle(request, null);
                            } else {
                                HttpServerResponse response = request.response();

                                int errorCode;
                                // if the error was set on the response use it
                                if (response.getStatusCode() >= 400) {
                                    errorCode = response.getStatusCode();
                                } else {
                                    // if it was set as the error object use it
                                    if (error instanceof Number) {
                                        errorCode = ((Number) error).intValue();
                                    } else {
                                        // default error code
                                        errorCode = 500;
                                    }
                                }

                                response.setStatusCode(errorCode);
                                response.setStatusMessage(HttpResponseStatus.valueOf(errorCode).reasonPhrase());
                                response.end(HttpResponseStatus.valueOf(errorCode).reasonPhrase());
                            }
                        }
                    }
                }.handle(null);
            }
        });
        return this;
    }
}
