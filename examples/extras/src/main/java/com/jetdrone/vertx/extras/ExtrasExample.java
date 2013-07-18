package com.jetdrone.vertx.extras;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.extras.engine.HandlebarsEngine;
import com.jetdrone.vertx.yoke.extras.middleware.JsonRestRouter;
import com.jetdrone.vertx.yoke.extras.store.MongoDbStore;
import com.jetdrone.vertx.yoke.middleware.*;
import com.jetdrone.vertx.yoke.middleware.BodyParser;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtrasExample extends Verticle {

    @Override
    public void start() {
        final Yoke yoke = new Yoke(vertx);
        yoke.engine("hbs", new HandlebarsEngine());

        yoke.use(new BodyParser());
        yoke.use(new ErrorHandler(true));

        JsonObject persistorCfg = new JsonObject();
        persistorCfg.putString("host", "localhost");
        persistorCfg.putNumber("port", 27017);
        persistorCfg.putString("address", "mongo.persons");
        persistorCfg.putString("db_name", "yoke");

        final EventBus eb = vertx.eventBus();

        // deploy mongo module
        container.deployModule("io.vertx~mod-mongo-persistor~2.0.0-final", persistorCfg);

        // db access
        final MongoDbStore db = new MongoDbStore(eb, "mongo.persons");

        JsonRestRouter router = new JsonRestRouter(db);
        router.rest("/persons", "persons");

        yoke.use(router);

        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {

                List<Map> users = new ArrayList<>();
                Map<String, String> user;

                user = new HashMap<>();
                user.put("username", "alan");
                user.put("firstName", "Alan");
                user.put("lastName", "Johnson");
                user.put("email", "alan@test.com");
                users.add(user);

                user = new HashMap<>();
                user.put("username", "allison");
                user.put("firstName", "Allison");
                user.put("lastName", "House");
                user.put("email", "allison@test.com");
                users.add(user);

                user = new HashMap<>();
                user.put("username", "ryan");
                user.put("firstName", "Ryan");
                user.put("lastName", "Carson");
                user.put("email", "ryan@test.com");
                users.add(user);

                request.put("users", users);
                request.response().render("views/handlebars.hbs");
            }
        });

        yoke.listen(8080);

        container.logger().info("Yoke server listening on port 8080");
    }
}
