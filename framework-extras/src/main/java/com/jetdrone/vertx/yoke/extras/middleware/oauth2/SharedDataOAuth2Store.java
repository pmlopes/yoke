package com.jetdrone.vertx.yoke.extras.middleware.oauth2;

import com.jetdrone.vertx.yoke.extras.middleware.OAuth2;
import com.jetdrone.vertx.yoke.util.YokeAsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;

public class SharedDataOAuth2Store implements OAuth2.Model {

    private final Set<String> authorizedClientIds;

    // {access_token: String, client_id: String, user_id: String, expires: Date}
    private final Set<String> oauth_access_tokens;
    // {client_id: String, client_secret: String, redirect_uri: String}
    private final Set<String> oauth_clients;
    // {user_id: String, username: String, password: String}
    private final Set<String> oauth_users;
    // {refresh_token: String, client_id: String, user_id: String, expires: Date}
    private final Set<String> oauth_refresh_tokens;

    public SharedDataOAuth2Store(Set<String> authorizedClientIds, Vertx vertx) {
        if (authorizedClientIds != null) {
            this.authorizedClientIds = authorizedClientIds;
        } else {
            this.authorizedClientIds = new HashSet<>();
        }

        oauth_access_tokens = vertx.sharedData().getSet("yoke.oauth.access.tokens");
        oauth_clients = vertx.sharedData().getSet("yoke.oauth.clients");
        oauth_users = vertx.sharedData().getSet("yoke.oauth.users");
        oauth_refresh_tokens = vertx.sharedData().getSet("yoke.oauth.refresh.tokens");
    }
    @Override
    public void getAccessToken(String access_token, AsyncResultHandler<JsonObject> handler) {
        if (oauth_access_tokens == null) {
            handler.handle(new YokeAsyncResult<JsonObject>(new NullPointerException()));
            return;
        }

        for (String data : oauth_access_tokens) {
            JsonObject json = new JsonObject(data);
            if (json.getString("access_token").equals(access_token)) {
                handler.handle(new YokeAsyncResult<>(null, json));
                return;
            }
        }

        handler.handle(new YokeAsyncResult<JsonObject>(null, null));
    }

    @Override
    public void getClient(String client_id, String client_secret, AsyncResultHandler<JsonObject> handler) {
        if (oauth_clients == null) {
            handler.handle(new YokeAsyncResult<JsonObject>(new NullPointerException()));
            return;
        }

        for (String data : oauth_clients) {
            JsonObject json = new JsonObject(data);
            if (json.getString("client_id").equals(client_id) && json.getString("client_secret").equals(client_secret)) {
                handler.handle(new YokeAsyncResult<>(null, json));
                return;
            }
        }

        handler.handle(new YokeAsyncResult<JsonObject>(null, null));
    }

    @Override
    public void grantTypeAllowed(String client_id, String grant_type, AsyncResultHandler<Boolean> handler) {
        if (grant_type.equals("password")) {
            if (authorizedClientIds.size() > 0) {
                handler.handle(new YokeAsyncResult<>(null, authorizedClientIds.contains(client_id.toLowerCase())));
                return;
            }
        }
        handler.handle(new YokeAsyncResult<>(null, true));
    }

    @Override
    public void getUser(String username, String password, AsyncResultHandler<JsonObject> handler) {
        if (oauth_users == null) {
            handler.handle(new YokeAsyncResult<JsonObject>(new NullPointerException()));
            return;
        }

        for (String data : oauth_users) {
            JsonObject json = new JsonObject(data);
            if (json.getString("username").equals(username) && json.getString("password").equals(password)) {
                handler.handle(new YokeAsyncResult<>(null, json));
                return;
            }
        }

        handler.handle(new YokeAsyncResult<JsonObject>(null, null));
    }

    @Override
    public void getRefreshToken(String refresh_token, AsyncResultHandler<JsonObject> handler) {
        if (oauth_refresh_tokens == null) {
            handler.handle(new YokeAsyncResult<JsonObject>(new NullPointerException()));
            return;
        }

        for (String data : oauth_refresh_tokens) {
            JsonObject json = new JsonObject(data);
            if (json.getString("access_token").equals(refresh_token)) {
                handler.handle(new YokeAsyncResult<>(null, json));
                return;
            }
        }

        handler.handle(new YokeAsyncResult<JsonObject>(null, null));
    }

    @Override
    public void revokeRefreshToken(String refresh_token, AsyncResultHandler<Void> handler) {
        if (oauth_refresh_tokens == null) {
            handler.handle(new YokeAsyncResult<Void>(new NullPointerException()));
            return;
        }

        for (String data : oauth_refresh_tokens) {
            JsonObject json = new JsonObject(data);
            if (json.getString("access_token").equals(refresh_token)) {
                oauth_refresh_tokens.remove(data);
                handler.handle(new YokeAsyncResult<Void>(null, null));
                return;
            }
        }
        // not found
        handler.handle(new YokeAsyncResult<Void>(null, null));
    }

    @Override
    public void saveAccessToken(String access_token, String client_id, String user_id, String expires, AsyncResultHandler<Void> handler) {
        if (oauth_access_tokens == null) {
            handler.handle(new YokeAsyncResult<Void>(new NullPointerException()));
            return;
        }

        JsonObject json = new JsonObject()
                .putString("access_tken", access_token)
                .putString("client_id", client_id)
                .putString("user_id", user_id)
                .putString("expires", expires);

        oauth_access_tokens.add(json.encode());
        handler.handle(new YokeAsyncResult<Void>(null, null));
    }

    @Override
    public void saveRefreshToken(String refresh_token, String client_id, String user_id, String expires, AsyncResultHandler<Void> handler) {
        if (oauth_refresh_tokens == null) {
            handler.handle(new YokeAsyncResult<Void>(new NullPointerException()));
            return;
        }

        JsonObject json = new JsonObject()
                .putString("refresh_token", refresh_token)
                .putString("client_id", client_id)
                .putString("user_id", user_id)
                .putString("expires", expires);

        oauth_refresh_tokens.add(json.encode());
        handler.handle(new YokeAsyncResult<Void>(null, null));
    }
}
