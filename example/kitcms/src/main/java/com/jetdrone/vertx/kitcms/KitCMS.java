package com.jetdrone.vertx.kitcms;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.MimeType;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.*;
import io.netty.handler.codec.http.multipart.FileUpload;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import java.io.IOException;
import java.util.Map;

public class KitCMS extends Verticle {

    @Override
    public void start() {
        final Config config = new Config(container.config());
        final Logger logger = container.logger();
        final EventBus eb = vertx.eventBus();

        // deploy redis module
        container.deployModule("com.jetdrone~mod-redis-io~1.1.0-SNAPSHOT", config.getRedisConfig());

        // db access
        final Db db = new Db(eb, Config.REDIS_ADDRESS);

        final Yoke yoke = new Yoke(vertx);
        // register jMustache render engine
        yoke.engine("mustache", new MustacheEngine());

        // install the pretty error handler middleware
        yoke.use(new ErrorHandler(true));
        // install the favicon middleware
        yoke.use(new Favicon());
        // install custom middleware to identify the domain
        yoke.use(new com.jetdrone.vertx.yoke.Middleware() {
            @Override
            public void handle(YokeHttpServerRequest request, Handler<Object> next) {
                String host = request.headers().get("host");
                if (host == null) {
                    // there is no host header
                    next.handle(400);
                } else {
                    if (host.indexOf(':') != -1) {
                        host = host.substring(0, host.indexOf(':'));
                    }

                    Config.Domain found = null;

                    for (Config.Domain domain : config.domains) {
                        if (domain.pattern.matcher(host).find()) {
                            found = domain;
                            break;
                        }
                    }

                    if (found == null) {
                        // still no host found even with header present
                        next.handle(404);
                    } else {
                        request.put("domain", found);
                        next.handle(null);
                    }
                }
            }
        });
        // install the static file server
        // note that since we are mounting under /static the root for the static middleware
        // will always be prefixed with /static
        yoke.use("/static", new Static("."));
        // install the BasicAuth middleware
        // TODO: get it from config
        yoke.use("/admin", new BasicAuth("foo", "bar"));
        // install body parser for /admin requests
        yoke.use("/admin", new BodyParser());
        // install router for admin requests
        yoke.use(new Router() {{
            get("/admin", new Middleware() {
                @Override
                public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
                    final Config.Domain domain = (Config.Domain) request.get("domain");

                    db.keys(domain.namespace, new AsyncResultHandler<JsonArray>() {
                        @Override
                        public void handle(AsyncResult<JsonArray> asyncResult) {
                            if (asyncResult.failed()) {
                                next.handle(asyncResult.cause());
                            } else {
                                request.put("keys", asyncResult.result().toArray());
                                request.response().render("com/jetdrone/vertx/kitcms/views/admin.mustache", next);
                            }
                        }
                    });
                }
            });
            get("/admin/keys", new Middleware() {
                @Override
                public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
                    final Config.Domain domain = (Config.Domain) request.get("domain");

                    db.keys(domain.namespace, new AsyncResultHandler<JsonArray>() {
                        @Override
                        public void handle(AsyncResult<JsonArray> asyncResult) {
                            if (asyncResult.failed()) {
                                next.handle(asyncResult.cause());
                            } else {
                                request.response().putHeader("Content-Type", "application/json");
                                request.response().end(asyncResult.result().encode());
                            }
                        }
                    });
                }
            });
            get("/admin/get", new Middleware() {
                @Override
                public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
                    final Config.Domain domain = (Config.Domain) request.get("domain");
                    String key = request.params().get("key");

                    if (key == null) {
                        request.response().end("Missing key");
                        return;
                    }

                    db.get(domain.namespace, key, new AsyncResultHandler<String>() {
                        @Override
                        public void handle(AsyncResult<String> asyncResult) {
                            if (asyncResult.failed()) {
                                next.handle(asyncResult.cause());
                            } else {
                                request.response().putHeader("Content-Type", "application/json");
                                // TODO: escape
                                request.response().end("\"" + asyncResult.result() + "\"");
                            }
                        }
                    });
                }
            });
            post("/admin/set", new Middleware() {
                @Override
                public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
                    final Config.Domain domain = (Config.Domain) request.get("domain");

                    Map<String, String> body = request.mapBody();

                    String key = body.get("key");
                    String value = body.get("value");

                    if (key == null) {
                        request.response().end("Missing key");
                        return;
                    }

                    if (value == null) {
                        request.response().end("Missing value");
                        return;
                    }

                    db.set(domain.namespace, key, value, new AsyncResultHandler<Void>() {
                        @Override
                        public void handle(AsyncResult<Void> asyncResult) {
                            if (asyncResult.failed()) {
                                next.handle(asyncResult.cause());
                            } else {
                                request.response().putHeader("Content-Type", "application/json");
                                request.response().end("\"OK\"");
                            }
                        }
                    });
                }
            });
            post("/admin/unset", new Middleware() {
                @Override
                public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
                    final Config.Domain domain = (Config.Domain) request.get("domain");

                    Map<String, String> body = request.mapBody();

                    String key = body.get("key");

                    if (key == null) {
                        request.response().end("Missing key");
                        return;
                    }

                    db.unset(domain.namespace, key, new AsyncResultHandler<Void>() {
                        @Override
                        public void handle(AsyncResult<Void> asyncResult) {
                            if (asyncResult.failed()) {
                                next.handle(asyncResult.cause());
                            } else {
                                request.response().putHeader("Content-Type", "application/json");
                                request.response().end("\"OK\"");
                            }
                        }
                    });
                }
            });
            get("/admin/export", new Middleware() {
                @Override
                public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
                    final Config.Domain domain = (Config.Domain) request.get("domain");

                    db.keys(domain.namespace, new AsyncResultHandler<JsonArray>() {
                        @Override
                        public void handle(AsyncResult<JsonArray> asyncResult) {
                            if (asyncResult.failed()) {
                                next.handle(asyncResult.cause());
                            } else {
                                // need to iterate all json array elements and get from redis
                                new AsyncIterator<Object>(asyncResult.result()) {

                                    final JsonArray buffer = new JsonArray();

                                    @Override
                                    public void handle(Object o) {
                                        if (!isEnd()) {
                                            final String key = (String) o;
                                            db.get(domain.namespace, key, new AsyncResultHandler<String>() {
                                                @Override
                                                public void handle(AsyncResult<String> asyncResult) {
                                                    if (asyncResult.failed()) {
                                                        next.handle(asyncResult.cause());
                                                    } else {
                                                        JsonObject json = new JsonObject();
                                                        json.putString("key", key);
                                                        json.putString("value", asyncResult.result());
                                                        buffer.addObject(json);

                                                        next();
                                                    }
                                                }
                                            });
                                        } else {
                                            YokeHttpServerResponse response = request.response();

                                            String filename = System.currentTimeMillis() + "_export.kit";

                                            response.putHeader("Content-Type", "application/json");
                                            response.putHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                                            response.end(buffer.encode());
                                        }
                                    }
                                };
                            }
                        }
                    });
                }
            });
            post("/admin/import", new Middleware() {
                @Override
                public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
                    final Config.Domain domain = (Config.Domain) request.get("domain");

                    FileUpload file = request.files().get("file");
                    try {
                        new AsyncIterator<Object>(new JsonArray(new String(file.get()))) {
                            @Override
                            public void handle(Object o) {
                                if (!isEnd()) {
                                    final JsonObject json = (JsonObject) o;
                                    db.set(domain.namespace, json.getString("key"), json.getString("value"), new AsyncResultHandler<Void>() {
                                        @Override
                                        public void handle(AsyncResult<Void> asyncResult) {
                                            if (asyncResult.failed()) {
                                                next.handle(asyncResult.cause());
                                            } else {
                                                next();
                                            }
                                        }
                                    });
                                } else {
                                    request.response().redirect("/admin");
                                }
                            }
                        };
                    } catch (IOException ioex) {
                        next.handle(ioex);
                    }
                }
            });
        }});

        // if the request fall through it is a view to render from the db
        yoke.use(new Middleware() {
            @Override
            public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
                final Config.Domain domain = (Config.Domain) request.get("domain");
                final String file = request.path().toLowerCase();

                db.get(domain.namespace, file, new AsyncResultHandler<String>() {
                    @Override
                    public void handle(AsyncResult<String> asyncResult) {
                        if (asyncResult.failed()) {
                            next.handle(asyncResult.cause());
                        } else {
                            if (asyncResult.result() == null) {
                                // if nothing is found on the database proceed with the chain
                                next.handle(null);
                            } else {
                                request.response().putHeader("Content-Type", MimeType.getMime(file, "text/html"));
                                request.response().end(asyncResult.result());
                            }
                        }
                    }
                });
            }
        });

        yoke.listen(config.serverPort, config.serverAddress);
        logger.info("Vert.x Server listening on " + config.serverAddress + ":" + config.serverPort);
    }
}
