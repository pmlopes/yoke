// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.middleware
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.logging.Logger;

import java.util.concurrent.ConcurrentMap;

/**
* Use it to secure EventBus Bridge. The message is authorised if sessionID
* is in the storage. You can manage the session storage at your proposal, 
* currently it only support shared Map.<p>
*
* Please see vert.x doc on how to use secured EventBus Bridge.<p>
*/
public class BridgeSecureHandler extends Middleware {

    private static final String DEFAULT_AUTH_ADDRESS = "yoke.basicauthmanager.authorise";

    private final String authAddress;
    private final String sessionStorage;

    @Override
    public Middleware init(final Vertx vertx, final Logger logger) {
        super.init(vertx, logger);

        vertx.eventBus().registerHandler(authAddress, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                // TODO: storage should be abstract to allow async storages such as redis, mongo, sql, etc...
                final ConcurrentMap<String, String> storage = vertx.sharedData().getMap(sessionStorage);
                JsonObject json = new JsonObject();
                String sessionID = message.body().getString("sessionID");

                if (sessionID != null && storage.get(sessionID) != null) {
                    json.putString("status", "ok");
                    json.putString("username", storage.get(sessionID));
                } else {
                    json.putString("status", "denied");
                }

                message.reply(json);
            }
        });

        return this;
    }

    public BridgeSecureHandler(final String authAddress, final String sessionStorage) {
        this.authAddress = authAddress;
        this.sessionStorage = sessionStorage;
    }

    public BridgeSecureHandler(final String sessionStorage) {
        this(DEFAULT_AUTH_ADDRESS, sessionStorage);
    }

    @Override
    public void handle(final YokeRequest request, final Handler<Object> next) {
        next.handle(null);
    }
}
