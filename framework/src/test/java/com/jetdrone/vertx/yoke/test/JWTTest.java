package com.jetdrone.vertx.yoke.test;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.security.JWT;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import java.util.HashMap;
import java.util.UUID;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

public class JWTTest extends TestVerticle {

    @Test
    public void testJWT() {
        final Yoke yoke = new Yoke(this);
        JWT jwt = new JWT(yoke.security());
        testComplete();
    }

    @Test
    public void testJWT2() {
        Yoke yoke = new Yoke(this);
        JWT jwt = new JWT(yoke.security());

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

//    @Test
//    public void testJWT3() {
//        Yoke app = new Yoke(this);
//        app.keyStore("keystore.jceks", "aysist7", new JsonObject()
//                .putString("HS256", "go-aysist")
//                .putString("HS384", "go-aysist")
//                .putString("HS512", "go-aysist")
//                .putString("RS256", "go-aysist"));
//
//        JWT jwt = new JWT(app.security());
//
//        System.out.println(jwt.encode(new JsonObject("{\"name\":\"PullemanG\",\"uid\":88,\"iat\":1404914641483,\"exp\":1405001041483,\"grants\":[\"MANAGER\",\"ROSTER_PORTAL\"]}")));
//
//        JsonObject json = jwt.decode("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiUHVsbGVtYW5HIiwidWlkIjo4OCwiaWF0IjoxNDA0OTE0NjQxNDgzLCJleHAiOjE0MDUwMDEwNDE0ODMsImdyYW50cyI6WyJNQU5BR0VSIiwiUk9TVEVSX1BPUlRBTCJdfQ.sQmLG1-ADCcb57gPAvemqTiPNB1j1i0gnS9mugXeVw0");
//
//        testComplete();
//    }
}
