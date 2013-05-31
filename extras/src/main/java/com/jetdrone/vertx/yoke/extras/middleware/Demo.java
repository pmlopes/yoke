package com.jetdrone.vertx.yoke.extras.middleware;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.extras.store.MongoDbStore;
import com.jetdrone.vertx.yoke.middleware.BodyParser;
import com.jetdrone.vertx.yoke.middleware.Static;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class Demo extends Verticle {

    @Override
    public void start() {

        JsonObject persistorCfg = new JsonObject();
        persistorCfg.putString("host", "localhost");
        persistorCfg.putNumber("port", 27017);
        persistorCfg.putString("address", "mongo.persons");
        persistorCfg.putString("db_name", "yoke");

        final EventBus eb = vertx.eventBus();

        // deploy mongo module
        container.deployModule("io.vertx~mod-mongo-persistor~2.0.0-beta1", persistorCfg);

        // db access
        final MongoDbStore db = new MongoDbStore(eb, "mongo.persons", "persons");

        final Yoke yoke = new Yoke(vertx);
//        // install the pretty error handler middleware
//        yoke.use(new ErrorHandler(true));
        // install body parser
        yoke.use(new BodyParser());

        // rest
        yoke.use(new JsonRestStore(db, "/persons"));
        yoke.use(new Static("public"));
        // listen
        yoke.listen(8080);
    }
}
