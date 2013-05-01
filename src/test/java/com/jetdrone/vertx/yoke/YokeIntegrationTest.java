/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.middleware.*;
import org.junit.Test;
import org.vertx.java.core.CaseInsensitiveMultiMap;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class YokeIntegrationTest extends TestVerticle {

    @Test
    public void testResponseTime() {
        YokeTester yoke = new YokeTester(vertx);
        yoke.use(new ResponseTime());
        yoke.use(new Handler<YokeHttpServerRequest>() {
            @Override
            public void handle(YokeHttpServerRequest request) {
                request.response().end();
            }
        });

        yoke.request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                assertNotNull(resp.headers().get("x-response-time"));
                testComplete();
            }
        });
    }

    @Test
    public void testLimit() {
        YokeTester yoke = new YokeTester(vertx);
        yoke.use(new Limit(1000));

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "text/plain");
        headers.add("content-length", "1024");

        Buffer body = new Buffer(1024);

        for (int i=0; i < 1024; i++) {
            body.appendByte((byte) 'A');
        }

        yoke.request("GET", "/", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(413, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void testFavicon() {
        YokeTester yoke = new YokeTester(vertx);
        yoke.use(new Favicon());

        final Buffer icon = Utils.readResourceToBuffer(Favicon.class, "favicon.ico");

        yoke.request("GET", "/favicon.ico", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                assertArrayEquals(icon.getBytes(), resp.body.getBytes());
                testComplete();
            }
        });
    }
}
