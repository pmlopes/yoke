package com.jetdrone.vertx;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.TooBusy;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import java.security.SecureRandom;

import static org.vertx.testtools.VertxAssert.*;

public class TooBusyTest extends TestVerticle {

    @Test
    public void testIsTooBusy() throws Exception {

        final Yoke yoke = new Yoke(this);
        final TooBusy tooBusy = new TooBusy();
        yoke.use(tooBusy);
        yoke.use(new Middleware() {
            double cnt = 0;
            final SecureRandom rand = new SecureRandom();

            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                for (int i = 0; i < 200000; i++) {
                    cnt += rand.nextDouble();
                }

                request.response().end();
            }
        });

        final YokeTester tester = new YokeTester(yoke, false);

        vertx.setPeriodic(100, new Handler<Long>() {
            int i = 0;
            int some200 = 0;
            int some503 = 0;

            @Override
            public void handle(Long event) {
                tester.request("GET", "/", new Handler<Response>() {
                    @Override
                    public void handle(Response response) {
                        if (response.getStatusCode() == 200) {
                            some200++;
                        }
                        if (response.getStatusCode() == 503) {
                            some503++;
                        }
                    }
                });

                if (++i == 100) {
                    System.out.println("[200]: " + some200 + " [503]: " + some503);
                    assertTrue(some200 > 0);
                    assertTrue(some503 > 0);
                    testComplete();
                }
            }
        });
    }
}