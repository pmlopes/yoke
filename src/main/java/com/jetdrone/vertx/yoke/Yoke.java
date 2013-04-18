package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.middleware.YokeHttpServerRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Yoke {

    private final Vertx vertx;
    private HttpServer server;

    private final Map<String, Object> defaultContext = new HashMap<>();

    public Yoke(Vertx vertx) {
        this.vertx = vertx;
        defaultContext.put("title", "Yoke.IO");
    }

    private static class MountedMiddleware {
        final String mount;
        final Middleware middleware;

        MountedMiddleware(String mount, Middleware middleware) {
            this.mount = mount;
            this.middleware = middleware;
        }
    }

    private List<MountedMiddleware> middlewareList = new ArrayList<>();
    private Middleware errorHandler;

    public Yoke use(String route, Middleware middleware) {
        if (middleware.isErrorHandler()) {
            errorHandler = middleware;
        } else {
            middlewareList.add(new MountedMiddleware(route, middleware));
        }

        // share the common vertx
        middleware.setVertx(vertx);
        return this;
    }

    public Yoke use(Middleware middleware) {
        return use("/", middleware);
    }

    public Yoke use(String route, final Handler<HttpServerRequest> handler) {
        middlewareList.add(new MountedMiddleware(route, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        }));
        return this;
    }

    public Yoke use(Handler<HttpServerRequest> handler) {
        return use("/", handler);
    }

    public void set(String key, Object value) {
        defaultContext.put(key, value);
    }

    public void setHttpServer(HttpServer httpServer) {
        this.server = httpServer;
    }

    public HttpServer listen(int port) {
        return listen(port, "localhost");
    }

    public HttpServer listen(int port, String address) {
        if (server == null) {
            server = vertx.createHttpServer();
        }

        server.requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                // the context map is shared with all middlewares
                final YokeHttpServerRequest request = new YokeHttpServerRequest(req, defaultContext);

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
                                // reached the end and no handler was able to answer the request
                                request.response().setStatusCode(404);
                                errorHandler.handle(request, null);
                            }
                        } else {
                            request.put("error", error);
                            errorHandler.handle(request, null);
                        }
                    }
                }.handle(null);
            }
        });

        server.listen(port, address);
        return server;
    }
}
