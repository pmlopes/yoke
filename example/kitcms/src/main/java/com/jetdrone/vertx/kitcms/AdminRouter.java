package com.jetdrone.vertx.kitcms;

import com.jetdrone.vertx.kit.BaseVerticle;
import com.jetdrone.vertx.kit.KitRequest;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class AdminRouter {

    public static void install(final BaseVerticle verticle, final Middleware middleware) {

        final EventBus eb = verticle.getVertx().eventBus();

        verticle.route.get("/admin", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {
                final KitRequest kit = new KitRequest(request);

                middleware.domain(kit, new Handler<Void>() {

                    @Override
                    public void handle(Void _void) {
                        final Config.Domain domain = (Config.Domain) kit.context.get("domain");
                        verticle.httpBasicAuth(kit, "Authorization Required", domain.user, domain.password, new Handler<Void>() {
                            @Override
                            public void handle(Void _void) {

                                JsonObject keys = new JsonObject();
                                keys.putString("command", "keys");
                                keys.putString("pattern", domain.namespace + "&*");
                                eb.send(Config.REDIS_ADDRESS, keys, new Handler<Message<JsonObject>>() {
                                    @Override
                                    public void handle(Message<JsonObject> msg) {

                                        if (!"ok".equals(msg.body.getString("status"))) {
                                            kit.context.put("message", msg.body.getString("message"));
                                            verticle.error(kit, 500);
                                            return;
                                        }

                                        kit.context.put("keys", msg.body.getArray("value").toArray());
                                        verticle.render(kit, "/com/jetdrone/vertx/kitcms/views/admin.mustache");
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        verticle.route.get("/admin/keys", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {
                final KitRequest kit = new KitRequest(request);

                middleware.domain(kit, new Handler<Void>() {

                    @Override
                    public void handle(Void _void) {
                        final Config.Domain domain = (Config.Domain) kit.context.get("domain");
                        verticle.httpBasicAuth(kit, "Authorization Required", domain.user, domain.password, new Handler<Void>() {
                            @Override
                            public void handle(Void _void) {

                                JsonObject keys = new JsonObject();
                                keys.putString("command", "keys");
                                keys.putString("pattern", domain.namespace + "&*");
                                eb.send(Config.REDIS_ADDRESS, keys, new Handler<Message<JsonObject>>() {
                                    @Override
                                    public void handle(Message<JsonObject> msg) {

                                        if (!"ok".equals(msg.body.getString("status"))) {
                                            kit.context.put("message", msg.body.getString("message"));
                                            verticle.error(kit, 500);
                                            return;
                                        }

                                        kit.response.putHeader("Content-Type", "application/json");
                                        kit.response.end(msg.body.getArray("value").encode());
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        verticle.route.get("/admin/get", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {
                final KitRequest kit = new KitRequest(request);

                middleware.domain(kit, new Handler<Void>() {

                    @Override
                    public void handle(Void _void) {
                        final Config.Domain domain = (Config.Domain) kit.context.get("domain");
                        verticle.httpBasicAuth(kit, "Authorization Required", domain.user, domain.password, new Handler<Void>() {
                            @Override
                            public void handle(Void _void) {

                                String key = kit.request.params().get("key");

                                if (key == null) {
                                    kit.context.put("message", "Missing key!");
                                    verticle.error(kit, 400);
                                    return;
                                }

                                key = key.toLowerCase();

                                JsonObject keys = new JsonObject();
                                keys.putString("command", "get");
                                keys.putString("key", key);

                                System.out.println(keys.encode());
                                eb.send(Config.REDIS_ADDRESS, keys, new Handler<Message<JsonObject>>() {
                                    @Override
                                    public void handle(Message<JsonObject> msg) {

                                        if (!"ok".equals(msg.body.getString("status"))) {
                                            kit.context.put("message", msg.body.getString("message"));
                                            verticle.error(kit, 500);
                                            return;
                                        }

                                        System.out.println(msg.body.encode());


                                        kit.response.putHeader("Content-Type", "application/json");
                                        kit.response.end(msg.body.getString("value"));
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        verticle.route.post("/admin/set", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {
                final KitRequest kit = new KitRequest(request);

                middleware.domain(kit, new Handler<Void>() {

                    @Override
                    public void handle(Void _void) {
                        final Config.Domain domain = (Config.Domain) kit.context.get("domain");
                        verticle.httpBasicAuth(kit, "Authorization Required", domain.user, domain.password, new Handler<Void>() {
                            @Override
                            public void handle(Void _void) {

                                kit.request.bodyHandler(new Handler<Buffer>() {
                                    public void handle(Buffer body) {
                                        // The entire body has now been received

                                        JsonObject json = new JsonObject(body.toString("UTF-8"));
                                        String key = json.getString("key");
                                        String value = json.getString("value");

                                        if (key == null) {
                                            kit.response.end("Missing key");
                                            return;
                                        }

                                        // TODO: if there are files get them...
                                        if (value == null) {
                                            kit.response.end("Missing value");
                                            return;
                                        }

                                        key = key.toLowerCase();

                                        JsonObject command = new JsonObject();
                                        command.putString("command", "set");
                                        command.putString("key", domain.namespace + "&" + key);
                                        command.putString("value", value);
                                        eb.send(Config.REDIS_ADDRESS, command, new Handler<Message<JsonObject>>() {
                                            @Override
                                            public void handle(Message<JsonObject> msg) {

                                                if (!"ok".equals(msg.body.getString("status"))) {
                                                    kit.context.put("message", msg.body.getString("message"));
                                                    verticle.error(kit, 500);
                                                    return;
                                                }

                                                kit.response.putHeader("Content-Type", "application/json");
                                                kit.response.end("\"OK\"");
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        verticle.route.post("/admin/unset", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {
                final KitRequest kit = new KitRequest(request);

                middleware.domain(kit, new Handler<Void>() {

                    @Override
                    public void handle(Void _void) {
                        final Config.Domain domain = (Config.Domain) kit.context.get("domain");
                        verticle.httpBasicAuth(kit, "Authorization Required", domain.user, domain.password, new Handler<Void>() {
                            @Override
                            public void handle(Void _void) {
                                kit.response.end("admin/unset");
                            }
                        });
                    }
                });
            }
        });
    }
}
