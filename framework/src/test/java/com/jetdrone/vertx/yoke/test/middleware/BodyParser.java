package com.jetdrone.vertx.yoke.test.middleware;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.middleware.Limit;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.Test;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class BodyParser extends TestVerticle {

    @Test
    public void testJsonBodyParser() {

        final JsonObject json = new JsonObject().putString("key", "value");

        Yoke yoke = new Yoke(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BodyParser());
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                assertNotNull(request.body());
                assertEquals(((JsonObject) request.body()).encode(), json.encode());
                request.response().end();
            }
        });

        Buffer body = new Buffer(json.encode());

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/json");
        headers.add("content-length", Integer.toString(body.length()));

        new YokeTester(yoke).request("POST", "/upload", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                assertNotNull(resp.body);
                testComplete();
            }
        });
    }

    @Test
    public void testMapBodyParser() {

        Yoke yoke = new Yoke(this);
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                MultiMap body = request.formAttributes();
                assertEquals("value", body.get("param"));
                request.response().end();
            }
        });

        Buffer body = new Buffer("param=value");

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/x-www-form-urlencoded");
        headers.add("content-length", Integer.toString(body.length()));

        new YokeTester(yoke).request("POST", "/upload", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                assertNotNull(resp.body);
                testComplete();
            }
        });
    }

    @Test
    public void testTextBodyParser() {

        Yoke yoke = new Yoke(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BodyParser());
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                Buffer body = request.body();
                assertEquals("hello-world", body.toString());
                request.response().end();
            }
        });

        Buffer body = new Buffer("hello-world");

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-length", Integer.toString(body.length()));

        new YokeTester(yoke).request("POST", "/upload", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                assertNotNull(resp.body);
                testComplete();
            }
        });
    }

    @Test
    public void testBodyParserWithEmptyBody() {

        Yoke yoke = new Yoke(this);
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().end();
            }
        });

        new YokeTester(yoke).request("DELETE", "/upload", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testJsonBodyLengthLimit() {

        Yoke yoke = new Yoke(this);
        yoke.use(new Limit(5L));
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BodyParser());
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                fail("Body should have been too long");
            }
        });

        Buffer body = new Buffer("[1,2,3,4,5]");

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/json");
        headers.add("transfer-encoding", "chunked");

        new YokeTester(yoke).request("POST", "/upload", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(413, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testTextBodyLengthLimit() {

        Yoke yoke = new Yoke(this);
        yoke.use(new Limit(5L));
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BodyParser());
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                fail("Body should have been too long");
            }
        });

        Buffer body = new Buffer("hello world");

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "plain/text");
        headers.add("transfer-encoding", "chunked");

        new YokeTester(yoke).request("POST", "/upload", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(413, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testFormEncodedBodyLengthLimit() {

        Yoke yoke = new Yoke(this);
        yoke.use(new Limit(5L));
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BodyParser());
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                fail("Body should have been too long");
            }
        });

        Buffer body = new Buffer("hello=world");

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED);
        headers.add("transfer-encoding", "chunked");

        new YokeTester(yoke).request("POST", "/upload", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(413, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testDeleteContentLengthZeroWithNoBody() {

        Yoke yoke = new Yoke(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BodyParser());
        yoke.use(new Handler<YokeRequest>() {
            @Override
            public void handle(YokeRequest request) {
                request.response().setStatusCode(204);
                request.response().end("");
            }
        });

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("Content-Type", "application/json");
        headers.add("Content-Length", "0");

        new YokeTester(yoke).request("DELETE", "/delete", headers, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(204, resp.getStatusCode());
                testComplete();
            }
        });
    }
}
