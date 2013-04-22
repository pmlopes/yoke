package com.jetdrone.vertx.kitcms;

import com.jetdrone.vertx.kit.BaseVerticle;
import com.jetdrone.vertx.kit.KitRequest;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.BasicAuth;
import com.jetdrone.vertx.yoke.middleware.ErrorHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.logging.Logger;

public class KitCMS extends BaseVerticle {
    @Override
    public void start() throws Exception {
        init(vertx);
        final Config config = new Config(getContainer().getConfig());
        final Logger logger = getContainer().getLogger();
        final Middleware middleware = new Middleware(this, config);

        // deploy redis module
        container.deployModule("com.jetdrone~mod-redis-io~1.1.0-SNAPSHOT", config.getRedisConfig(), new Handler<String>() {
            @Override
            public void handle(String event) {
                System.out.println(event);
            }
        });

        HttpServer server = vertx.createHttpServer();

        // add admin routes
        AdminRouter.install(this, middleware);

        route.allWithRegEx("\\/static\\/.*", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {
                final KitRequest kit = new KitRequest(request);
                resource(kit);
            }
        });
        

        route.noMatch(new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {
                final KitRequest kit = new KitRequest(request);

                System.out.println("no match!" + request.uri);
                middleware.domain(kit, new Handler<Void>() {
                    @Override
                    public void handle(Void _void) {
                        final Config.Domain domain = (Config.Domain) kit.context.get("domain");

                        String url = request.uri != null ? request.uri : "/";

                        // remove any trailing slashes
                        while(url.length() > 1 && "/".equals(url.substring(url.length() - 1))) {
                            url = url.substring(0, url.length() - 1);
                        }
                        url = url.toLowerCase();

                        // make sure url starts with a forward slash
                        if (!"/".equals(url.substring(0, 1))) {
                            request.response.statusCode = 404;
                            request.response.end();
                            return;
                        }

                        // Hand the url off to our renderizer
                        // TODO: render
                        request.response.end(url);
                    }
                });
            }
        });

        // routing config
        server.requestHandler(route);

        server.listen(config.serverPort, config.serverAddress);
        logger.info("Vert.x Server listening on " + config.serverAddress + ":" + config.serverPort);
    }
}
