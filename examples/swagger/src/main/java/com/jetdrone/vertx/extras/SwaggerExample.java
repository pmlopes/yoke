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

public class SwaggerExample extends Verticle {

    @Override
    public void start() {
        final Yoke yoke = new Yoke(this);

        yoke.use(new BodyParser());
        yoke.use(new ErrorHandler(true));

        final EventBus eb = vertx.eventBus();

        yoke.listen(8080);

        container.logger().info("Yoke server listening on port 8080");
    }
}
