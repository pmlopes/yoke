package com.jetdrone.vertx.yoke.extras.middleware;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.BodyParser;
import com.jetdrone.vertx.yoke.extras.stores.RedisStore;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class Demo extends Verticle {

    @Override
    public void start() {
        JsonObject config = new JsonObject();

        config.putString("address", "db.redis");
        config.putString("host", "localhost");
        config.putNumber("port", 6379);

        final EventBus eb = vertx.eventBus();

        // deploy redis module
        container.deployModule("com.jetdrone~mod-redis-io~1.1.0-beta3", config);

        // db access
        final RedisStore db = new RedisStore(eb, "db.redis", "persons");

        final Yoke yoke = new Yoke(vertx);
//        // install the pretty error handler middleware
//        yoke.use(new ErrorHandler(true));
        // install body parser
        yoke.use(new BodyParser());

        // rest
        yoke.use(new JsonRestStore(db, "/persons"));
        // listen
        yoke.listen(8080);
    }
}
