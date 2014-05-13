package com.jetdrone.vertx.yoke.test;

import com.jetdrone.vertx.yoke.security.YokeSecurity;
import com.jetdrone.vertx.yoke.util.JWT;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

public class JWTTest extends TestVerticle {

    @Test
    public void testJWT() {
        YokeSecurity security = new YokeSecurity();
        JWT jwt = new JWT(security, "secret");
        testComplete();
    }

    @Test
    public void testJWT2() {
        YokeSecurity security = new YokeSecurity();
        JWT jwt = new JWT(security, "s3cRE7");

        long now = System.currentTimeMillis();

        JsonObject json = new JsonObject()
                .putString("name", "Paulo Lopes")
                .putNumber("uid", 0)
                .putNumber("iat", now)
                .putNumber("exp", now + 24*60*60*1000)
                .putArray("claims", new JsonArray().add("a").add("b"));

        String token = jwt.encode(json);

        assertTrue(!token.contains("\n"));

        JsonObject decoded = jwt.decode(token);

        assertEquals("Paulo Lopes", decoded.getString("name"));
        assertEquals(0, decoded.getNumber("uid"));
        assertEquals(now, decoded.getNumber("iat"));
        assertEquals(now + 24*60*60*1000, decoded.getNumber("exp"));

        testComplete();
    }
}
