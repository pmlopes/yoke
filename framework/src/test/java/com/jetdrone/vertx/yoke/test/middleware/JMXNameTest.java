package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import io.vertx.core.Handler;

import java.util.regex.Pattern;

public class JMXNameTest {

    @Test
    public void issue121() {
        //route everything start with hello
        Pattern p = Pattern.compile("^/hello(/|$).*");
        new com.jetdrone.vertx.yoke.middleware.Router().get(p, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {

            }
        });
    }
}
