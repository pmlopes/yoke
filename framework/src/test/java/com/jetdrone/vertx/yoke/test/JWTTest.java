package com.jetdrone.vertx.yoke.test;

import com.jetdrone.vertx.yoke.util.JWT;
import org.junit.Test;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.testComplete;

public class JWTTest extends TestVerticle {

    @Test
    public void testJWT() {
        JWT jwt = new JWT("secret");
        testComplete();
    }
}
