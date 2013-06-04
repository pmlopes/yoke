package com.jetdrone.vertx.extras;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.engine.StringPlaceholderEngine;
import com.jetdrone.vertx.yoke.middleware.*;
import com.jetdrone.vertx.yoke.util.Utils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import javax.crypto.Mac;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExtrasExample extends Verticle {

    @Override
    public void start() {
        final Yoke yoke = new Yoke(vertx);
        yoke.engine("hbs", new StringPlaceholderEngine());

        yoke.listen(8080);
        container.logger().info("Yoke server listening on port 8080");
    }
}
