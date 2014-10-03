/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.AbstractMiddleware;
import com.jetdrone.vertx.yoke.Middleware;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.store.SessionStore;
import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.eventbus.Message;

/** # BridgeSecureHandler
 *
 * Use it to secure EventBus Bridge. The message is authorised if sessionID
 * is in the storage. You can manage the session storage at your proposal.
 *
 * Please see vert.x doc on how to use secured EventBus Bridge.
 */
public class BridgeSecureHandler extends AbstractMiddleware {

    /** Default Address if none specified
     */
    private static final String DEFAULT_AUTH_ADDRESS = "yoke.basicauthmanager.authorise";

    /** The address the bridge is listening
     */
    private final String authAddress;

    /** Session storage key
     */
    private final SessionStore sessionStore;

    @Override
    public Middleware init(@NotNull final Yoke yoke, @NotNull final String mount) {
        super.init(yoke, mount);

        // register a new handler for the configured address
        eventBus().registerHandler(authAddress, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(final Message<JsonObject> message) {
                final JsonObject json = new JsonObject();
                String sessionID = message.body().getString("sessionID");

                if (sessionID == null) {
                    json.putString("status", "denied");
                    message.reply(json);
                    return;
                }

                sessionStore.get(sessionID, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject session) {
                        if (session == null) {
                            json.putString("status", "denied");
                            message.reply(json);
                            return;
                        }

                        json.putString("status", "ok");
                        json.putString("username", session.getString("username"));
                        message.reply(json);
                    }
                });
            }
        });

        return this;
    }

    /** Creates a new BridgeSecureHandler
     *
     * @param authAddress event buss address for authentication module
     * @param sessionStore the store where the session is to be located from
     */
    public BridgeSecureHandler(@NotNull final String authAddress, @NotNull final SessionStore sessionStore) {
        this.authAddress = authAddress;
        this.sessionStore = sessionStore;
    }

    /** Creates a new BridgeSecureHandler using the default auth address
     *
     * @param sessionStore the store where the session is to be located from
     */
    public BridgeSecureHandler(@NotNull final SessionStore sessionStore) {
        this(DEFAULT_AUTH_ADDRESS, sessionStore);
    }

    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
        next.handle(null);
    }
}
