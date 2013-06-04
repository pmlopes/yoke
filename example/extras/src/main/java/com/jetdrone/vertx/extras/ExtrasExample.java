package com.jetdrone.vertx.extras;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.extras.engine.HandlebarsEngine;
import com.jetdrone.vertx.yoke.extras.middleware.JsonRestRouter;
import com.jetdrone.vertx.yoke.extras.store.MongoDbStore;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class ExtrasExample extends Verticle {

    @Override
    public void start() {
        final Yoke yoke = new Yoke(vertx);
        yoke.engine("hbs", new HandlebarsEngine());

        JsonObject persistorCfg = new JsonObject();
        persistorCfg.putString("host", "localhost");
        persistorCfg.putNumber("port", 27017);
        persistorCfg.putString("address", "mongo.persons");
        persistorCfg.putString("db_name", "yoke");

        final EventBus eb = vertx.eventBus();

        // deploy mongo module
        container.deployModule("io.vertx~mod-mongo-persistor~2.0.0-beta1", persistorCfg);

        // db access
        final MongoDbStore db = new MongoDbStore(eb, "mongo.persons");

        JsonRestRouter router = new JsonRestRouter(db);
        router.rest("/persons", "persons");

        yoke.use(router);
        yoke.listen(8080);

        container.logger().info("Yoke server listening on port 8080");
    }
}
