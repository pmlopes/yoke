/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.core.Context;
import com.jetdrone.vertx.yoke.core.MountedMiddleware;
import com.jetdrone.vertx.yoke.core.RequestWrapper;
import com.jetdrone.vertx.yoke.core.impl.DefaultRequestWrapper;
import com.jetdrone.vertx.yoke.jmx.ContextMBean;
import com.jetdrone.vertx.yoke.jmx.MiddlewareMBean;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.security.KeyStoreSecurity;
import com.jetdrone.vertx.yoke.security.SecretSecurity;
import com.jetdrone.vertx.yoke.store.SessionStore;
import com.jetdrone.vertx.yoke.store.SharedDataSessionStore;
import com.jetdrone.vertx.yoke.core.YokeException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.jetbrains.annotations.*;

import javax.management.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * # Yoke
 *
 * Yoke is a chain executor of middleware for Vert.x 2.x. The goal of this library is not to provide a web application
 * framework but the backbone that helps the creation of web applications.
 *
 * Yoke works in a similar way to Connect middleware. Users start by declaring which middleware components want to use
 * and then start an http server either managed by Yoke or provided by the user (say when you need https).
 *
 * Yoke has no extra dependencies than Vert.x itself so it is self contained.
 */
public class Yoke {

    //Get the MBean server
    private final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

    /**
     * Vert.x instance
     */
    private final Vertx vertx;

    /**
     * request wrapper in use
     */
    private final RequestWrapper requestWrapper;

    /**
     * default context used by all requests
     *
     * <pre>
     * {
     *   title: "Yoke",
     *   x-powered-by: true,
     *   trust-proxy: true
     * }
     * </pre>
     */
    protected final Map<String, Object> defaultContext = new HashMap<>();

    /**
     * The internal registry of [render engines](Engine.html)
     */
    private final Map<String, Engine> engineMap = new HashMap<>();

    /**
     * Creates a Yoke instance.
     *
     * This constructor should be called from a verticle and pass a valid Vertx instance. This instance will be shared
     * with all registered middleware. The reason behind this is to allow middleware to use Vertx features such as file
     * system and timers.
     *
     * <pre>
     * public class MyVerticle extends AbstractVerticle {
     *   public void start() {
     *     final Yoke yoke = new Yoke(this);
     *     ...
     *   }
     * }
     * </pre>
     *
     * @param verticle the main verticle
     */
    public Yoke(@NotNull Verticle verticle) {
        this(verticle.getVertx());
    }

    /**
     * Creates a Yoke instance.
     *
     * This constructor should be called from a verticle and pass a valid Vertx instance and a Logger. This instance
     * will be shared with all registered middleware. The reason behind this is to allow middleware to use Vertx
     * features such as file system and timers.
     *
     * <pre>
     * public class MyVerticle extends AbstractVerticle {
     *   public void start() {
     *     final Yoke yoke = new Yoke(getVertx());
     *     ...
     *   }
     * }
     * </pre>
     *
     * @param vertx
     */
    public Yoke(@NotNull Vertx vertx) {
        this(vertx, new DefaultRequestWrapper());
    }

    /**
     * Creates a Yoke instance.
     *
     * This constructor should be called internally or from other language bindings.
     *
     * <pre>
     * public class MyVerticle extends AbstractVerticle {
     *   public void start() {
     *     final Yoke yoke = new Yoke(getVertx(),
     *         new RequestWrapper() {...});
     *     ...
     *   }
     * }
     * </pre>
     *
     * @param vertx
     * @param requestWrapper
     */
    public Yoke(@NotNull Vertx vertx, @NotNull RequestWrapper requestWrapper) {
        this.vertx = vertx;
        this.requestWrapper = requestWrapper;
        defaultContext.put("title", "Yoke");
        defaultContext.put("x-powered-by", true);
        defaultContext.put("trust-proxy", true);
        store = new SharedDataSessionStore(vertx, "yoke.sessiondata");

        // register on JMX
        try {
            mbs.registerMBean(new ContextMBean(defaultContext), new ObjectName("com.jetdrone.yoke:instance=@" + hashCode() + ",type=DefaultContext@" + defaultContext.hashCode()));
        } catch (InstanceAlreadyExistsException e) {
            // ignore
        } catch (MalformedObjectNameException | MBeanRegistrationException | NotCompliantMBeanException e) {
            throw new RuntimeException(e);
        }
    }

    public Vertx vertx() {
        return vertx;
    }

    /**
     * Ordered list of mounted middleware in the chain
     */
    private final List<MountedMiddleware> middlewareList = new ArrayList<>();

    /**
     * Special middleware used for error handling
     */
    private IMiddleware errorHandler;

    /**
     * Adds a IMiddleware to the chain. If the middleware is an Error Handler IMiddleware then it is
     * treated differently and only the last error handler is kept.
     *
     * You might want to add a middleware that is only supposed to run on a specific route (path prefix).
     * In this case if the request path does not match the prefix the middleware is skipped automatically.
     *
     * <pre>
     * yoke.use("/login", new CustomLoginMiddleware());
     * </pre>
     *
     * @param route      The route prefix for the middleware
     * @param middleware The middleware add to the chain
     */
    public Yoke use(@NotNull String route, @NotNull IMiddleware... middleware) {
        for (IMiddleware m : middleware) {
            if (m instanceof Middleware) {
                // when the type of middleware is error handler then the route is ignored and
                // the middleware is extracted from the execution chain into a special placeholder
                // for error handling
                if (((Middleware) m).isErrorHandler()) {
                    errorHandler = m;
                } else {
                    MountedMiddleware mm = new MountedMiddleware(route, m);
                    middlewareList.add(mm);

                    // register on JMX
                    try {
                        mbs.registerMBean(new MiddlewareMBean(mm), new ObjectName("com.jetdrone.yoke:type=Middleware@" + hashCode() + ",route=" + ObjectName.quote(route) + ",name=" + m.getClass().getSimpleName() + "@" + m.hashCode()));
                    } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
                        throw new RuntimeException(e);
                    }
                }

                // initialize the middleware with the current Vert.x and Logger
                ((Middleware) m).init(this, route);
            } else {
                MountedMiddleware mm = new MountedMiddleware(route, m);
                middlewareList.add(mm);

                // register on JMX
                try {
                    mbs.registerMBean(new MiddlewareMBean(mm), new ObjectName("com.jetdrone.yoke:type=Middleware@" + hashCode() + ",route=" + ObjectName.quote(route) + ",name=" + m.getClass().getSimpleName() + "@" + m.hashCode()));
                } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return this;
    }

    /**
     * Adds a middleware to the chain with the prefix "/".
     *
     * @param middleware The middleware add to the chain
     */
    public Yoke use(@NotNull IMiddleware... middleware) {
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
     * <pre>
     * yoke.use("/login", new Handler&lt;YokeRequest&gt;() {
     *   public void handle(YokeRequest request) {
     *     request.response.end("Hello");
     *   }
     * });
     * </pre>
     *
     * @param route   The route prefix for the middleware
     * @param handler The Handler to add
     */
    public Yoke use(@NotNull String route, final @NotNull Handler<YokeRequest> handler) {
        middlewareList.add(new MountedMiddleware(route, new IMiddleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        }));
        return this;
    }

    /**
     * Adds a Handler to a route.
     *
     * <pre>
     * yoke.use("/login", new Handler&lt;YokeRequest&gt;() {
     *   public void handle(YokeRequest request) {
     *     request.response.end("Hello");
     *   }
     * });
     * </pre>
     * @param handler The Handler to add
     */
    public Yoke use(@NotNull Handler<YokeRequest> handler) {
        return use("/", handler);
    }

    /**
     * Adds a Render Engine to the library. Render Engines are Template engines you
     * might want to use to speed the development of your application. Once they are
     * registered you can use the method render in the YokeResponse to
     * render a template.
     *
     * @param engine    The implementation of the engine
     */
    public Yoke engine(@NotNull Engine engine) {
        engine.setVertx(vertx);
        engineMap.put(engine.extension(), engine);
        return this;
    }

    /**
     * Special store engine used for accessing session data
     */
    protected SessionStore store;

    public Yoke store(@NotNull SessionStore store) {
        this.store = store;
        return this;
    }

    protected YokeSecurity security;

    public Yoke keyStoreSecurity(@NotNull final String fileName, @NotNull final String keyStorePassword, @NotNull final JsonObject keyPasswords) {
        String storeType;
        int idx = fileName.lastIndexOf('.');

        if (idx == -1) {
            storeType = KeyStore.getDefaultType();
        } else {
            storeType = fileName.substring(idx + 1);
        }

        try {
            KeyStore ks = KeyStore.getInstance(storeType);

            try (InputStream in = new FileInputStream(((VertxInternal) vertx).resolveFile(fileName))) {
                ks.load(in, keyStorePassword.toCharArray());
            }

            this.security = new KeyStoreSecurity(ks, keyPasswords.getMap());

        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Yoke keyStoreSecurity(@NotNull final String fileName, @NotNull final String keyStorePassword) {
        String storeType;
        int idx = fileName.lastIndexOf('.');

        if (idx == -1) {
            storeType = KeyStore.getDefaultType();
        } else {
            storeType = fileName.substring(idx + 1);
        }

        try {
            KeyStore ks = KeyStore.getInstance(storeType);

            try (InputStream in = new FileInputStream(((VertxInternal) vertx).resolveFile(fileName))) {
                ks.load(in, keyStorePassword.toCharArray());
            }

            this.security = new KeyStoreSecurity(ks, keyStorePassword);

        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Yoke secretSecurity(@NotNull final String secret) {
        this.security = new SecretSecurity(secret);
        return this;
    }

    public Yoke secretSecurity(@NotNull final byte[] secret) {
        this.security = new SecretSecurity(secret);
        return this;
    }

    public YokeSecurity security() {
        if (security == null) {
            throw new RuntimeException("No YokeSecurity implementation is enabled!");
        }

        return security;
    }

    /**
     * When you need to share global properties with your requests you can add them
     * to Yoke and on every request they will be available as request.get(String)
     *
     * @param key   unique identifier
     * @param value Any non null value, nulls are not saved
     */
    public Yoke set(@NotNull String key, Object value) {
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
     * @return {Yoke}
     */
    public Yoke listen(int port) {
        return listen(port, "0.0.0.0", null);
    }

    /**
     * Starts the server listening at a given port bind to all available interfaces.
     *
     * @param port    the server TCP port
     * @param handler for asynchronous result of the listen operation
     * @return {Yoke}
     */
    public Yoke listen(int port, @NotNull Handler<Boolean> handler) {
        return listen(port, "0.0.0.0", handler);
    }

    /**
     * Starts the server listening at a given port and given address.
     *
     * @param port the server TCP port
     * @return {Yoke}
     */
    public Yoke listen(int port, @NotNull String address) {
        return listen(port, address, null);
    }

    /**
     * Starts the server listening at a given port and given address.
     *
     * @param port    the server TCP port
     * @param handler for asynchronous result of the listen operation
     * @return {Yoke}
     */
    public Yoke listen(int port, @NotNull String address, final Handler<Boolean> handler) {
        HttpServer server = vertx.createHttpServer();

        listen(server);

        if (handler != null) {
            server.listen(port, address, new Handler<AsyncResult<HttpServer>>() {
                @Override
                public void handle(AsyncResult<HttpServer> listen) {
                    handler.handle(listen.succeeded());
                }
            });
        } else {
            server.listen(port, address);
        }
        return this;
    }

    /**
     * Starts listening at a already created server.
     *
     * @param server
     * @return {Yoke}
     */
    public Yoke listen(final @NotNull HttpServer server) {
        server.requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest req) {
                // the context map is shared with all middlewares
                final YokeRequest request = requestWrapper.wrap(req, new Context(defaultContext), engineMap, store);

                // add x-powered-by header is enabled
                Boolean poweredBy = request.get("x-powered-by");
                if (poweredBy != null && poweredBy) {
                    request.response().putHeader("x-powered-by", "yoke");
                }

                new Handler<Object>() {
                    int currentMiddleware = -1;

                    @Override
                    public void handle(Object error) {
                        if (error == null) {
                            currentMiddleware++;
                            if (currentMiddleware < middlewareList.size()) {
                                final MountedMiddleware mountedMiddleware = middlewareList.get(currentMiddleware);

                                if (!mountedMiddleware.enabled) {
                                    // the middleware is disabled
                                    handle(null);
                                } else {
                                    if (request.path().startsWith(mountedMiddleware.mount)) {
                                        mountedMiddleware.middleware.handle(request, this);
                                    } else {
                                        // the middleware was not mounted on this uri, skip to the next entry
                                        handle(null);
                                    }
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
                                    } else if (error instanceof YokeException) {
                                        errorCode = ((YokeException) error).getErrorCode().intValue();
                                    } else if (error instanceof JsonObject) {
                                        errorCode = ((JsonObject) error).getInteger("errorCode", 500);
                                    } else if (error instanceof Map) {
                                        Integer tmp = (Integer) ((Map) error).get("errorCode");
                                        errorCode = tmp != null ? tmp : 500;
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

    /**
     * Deploys required middleware from a config json element.
     *
     * The current format for the config is either a single item or an array:
     * <pre>
     * [{
     *   module: String, // the name of the module
     *   verticle: String, // the name of the verticle (either verticle or module must be present)
     *   instances: Number, // how many instances, default 1
     *   worker: Boolean, // is it a worker verticle? default false
     *   multiThreaded: Boolean, // is it a multiThreaded verticle? default false
     *   config: JsonObject // any config you need to pass to the module/verticle
     * }]
     * </pre>
     *
     * @param config a json array.
     */
    public Yoke deploy(@NotNull JsonArray config) {
        return deploy(config, null);
    }

    /**
     * Deploys required middleware from a config json element. The handler is only called once all middleware is
     * deployed or in error. The order of deployment is not guaranteed since all deploy functions are called
     * concurrently and do not wait for the previous result before deploying the next item.
     *
     * The current format for the config is either a single item or an array:
     * <pre>
     * {
     *   module: String, // the name of the module
     *   verticle: String, // the name of the verticle (either verticle or module must be present)
     *   instances: Number, // how many instances, default 1
     *   worker: Boolean, // is it a worker verticle? default false
     *   multiThreaded: Boolean, // is it a multiThreaded verticle? default false
     *   config: JsonObject // any config you need to pass to the module/verticle
     * }
     * </pre>
     *
     * @param config either a json object or a json array.
     * @param handler A handler that is called once all middleware is deployed or on error.
     */
    public Yoke deploy(final @NotNull JsonArray config, final Handler<Object> handler) {

        if (config.size() == 0) {
            if (handler == null) {
                return this;
            } else {
                handler.handle(null);
                return this;
            }
        }

        // wait for all deployments before calling the real handler
        Handler<AsyncResult<String>> waitFor = new Handler<AsyncResult<String>>() {

            private int latch = config.size();
            private boolean handled = false;

            @Override
            public void handle(AsyncResult<String> event) {
                latch--;
                if (handler != null) {
                    if (!handled && (event.failed() || latch == 0)) {
                        handled = true;
                        handler.handle(event.failed() ? event.cause() : null);
                    }
                }
            }
        };

        for (Object o : config) {
            JsonObject json = (JsonObject) o;
            vertx.deployVerticle(json.getString("verticle"), new DeploymentOptions(json), waitFor);
        }

        return this;
    }
}
