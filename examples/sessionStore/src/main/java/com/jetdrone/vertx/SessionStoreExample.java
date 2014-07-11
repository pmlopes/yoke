package com.jetdrone.vertx;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.*;
import com.jetdrone.vertx.yoke.store.MongoDBSessionStore;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import javax.crypto.Mac;

public class SessionStoreExample extends Verticle {

    @Override
    public void start() {
        super.start();

        // load the general config object, loaded by using -config on command line
        JsonObject appConfig = new JsonObject()
                .putString("db_name", "test");

        // deploy the mongo-persistor module, which we'll use for persistence
        container.deployModule("io.vertx~mod-mongo-persistor~2.1.0", appConfig, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> deploymentId) {
                if (deploymentId == null) {
                    System.err.println("Deployment failed!");
                    System.exit(1);
                }

                final Yoke app = new Yoke(SessionStoreExample.this);
                app.secretSecurity("keyboard cat");

                app.store(new MongoDBSessionStore(vertx.eventBus(), "vertx.mongopersistor", "sessions"));

                final Mac hmac = app.security().getMac("HmacSHA256");

                app.use(new BodyParser());
                app.use(new CookieParser(hmac));
                app.use(new Session(hmac));


                app.use(new Router() {{
                    get("/", new Handler<YokeRequest>() {
                        @Override
                        public void handle(YokeRequest request) {
                            JsonObject session = request.get("session");
                            if (session == null) {
                                request.response().setStatusCode(404);
                                request.response().end();
                            } else {
                                request.response().end(session);
                            }
                        }
                    });

                    get("/new", new Handler<YokeRequest>() {
                        @Override
                        public void handle(YokeRequest request) {
                            JsonObject session = request.createSession();

                            session.putString("key", "value");

                            request.response().end();
                        }
                    });

                    get("/delete", new Handler<YokeRequest>() {
                        @Override
                        public void handle(YokeRequest request) {
                            request.destroySession();
                            request.response().end();
                        }
                    });
                }});

                app.listen(8000);
            }
        });
    }
}