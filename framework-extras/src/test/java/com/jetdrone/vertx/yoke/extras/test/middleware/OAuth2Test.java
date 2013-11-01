package com.jetdrone.vertx.yoke.extras.test.middleware;

import com.jetdrone.vertx.yoke.extras.middleware.OAuth2;
import com.jetdrone.vertx.yoke.extras.middleware.oauth2.SharedDataOAuth2Store;
import com.jetdrone.vertx.yoke.middleware.BodyParser;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.impl.CaseInsensitiveMultiMap;
import org.vertx.testtools.TestVerticle;

import java.util.Set;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.testComplete;

public class OAuth2Test extends TestVerticle {

    @Test
    public void oauthTest() {

        // populate the shared data

        // {client_id: String, client_secret: String, redirect_uri: String}
        Set<String> oauth_clients = vertx.sharedData().getSet("yoke.oauth.clients");
        // {user_id: String, username: String, password: String}
        Set<String> oauth_users = vertx.sharedData().getSet("yoke.oauth.users");

        oauth_clients.add("{\"client_id\":\"client_id\",\"client_secret\":\"client_secret\",\"redirect_uri\":\"redirect_uri\"}");
        oauth_users.add("{\"user_id\":\"user_id\",\"username\":\"username\",\"password\":\"password\"}");


        YokeTester yoke = new YokeTester(this);
        yoke.use(new BodyParser());
        yoke.use(new OAuth2(new SharedDataOAuth2Store(null, vertx)));

        // create a request
        Buffer body = new Buffer("grant_type=password&username=username&password=password&client_id=client_id&client_secret=client_secret");

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/x-www-form-urlencoded");
        headers.add("content-length", Integer.toString(body.length()));


        yoke.request("POST", "/oauth/token", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                System.out.println(resp.getStatusMessage());
                assertEquals(200, resp.getStatusCode());
                assertNotNull(resp.body);
                System.out.println(resp.body.toString());
                testComplete();
            }
        });


    }
}
