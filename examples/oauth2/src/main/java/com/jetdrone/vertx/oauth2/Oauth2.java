package com.jetdrone.vertx.oauth2;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.extras.middleware.OAuth2Provider;
import com.jetdrone.vertx.yoke.middleware.*;
import com.jetdrone.vertx.yoke.util.Utils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import javax.crypto.Mac;

public class Oauth2 extends Verticle {

    @Override
    public void start() {

        Mac mac = Utils.newHmacSHA256("abracadabra");
        OAuth2Provider oauthProvider = new OAuth2Provider("signing-secret");

//oauthProvider.on('authorizeParamMissing', function(req, res, callback) {
//	res.writeHead(400);
//	res.end("missing param");
//});
//
//oauthProvider.on('enforceLogin', function(req, res, authorizeUrl, callback) {
//	if(req.session.user) {
//		callback(req.session.user);
//	} else {
//		res.writeHead(303, {Location: '/login?next=' + encodeURIComponent(authorizeUrl)});
//		res.end();
//	}
//});
//
//oauthProvider.on('shouldSkipAllow', function(userId, clientId, callback){
//	callback();
//});
//
//oauthProvider.on('validateClientIdAndRedirectUri', function(clientId, redirectUri, req, res, callback) {
//	callback();
//});
//
//oauthProvider.on('authorizeForm', function(req, res, clientId, authorizeUrl) {
//	res.end('<html>this app wants to access your account... <form method="post" action="' + authorizeUrl + '"><button name="allow" value="true">Allow</button></form>');
//});
//
//oauthProvider.on('invalidResponseType', function(req, res, callback) {
//	res.writeHead(400);
//	res.end("invalid response type");
//});
//
//oauthProvider.on('accessDenied', function(req, res, callback) {
//	res.json(401, {error:"access denied"});
//});
//
//oauthProvider.on('createAccessToken', function(userId, clientId, callback) {
//	callback("test-tooken");
//});
//
//oauthProvider.on('createGrant', function(req, clientId, callback) {
//	callback("ABC123");
//});
//
//oauthProvider.on('lookupGrant', function(clientId, clientSecret, code, res, callback) {
//	callback("userId");
//});

        final Yoke app = new Yoke(this);

        app.use(new ErrorHandler(true));

        app.use(new Logger());
        app.use(new BodyParser());
        app.use(new CookieParser());
        app.use(new Session(mac));
        app.use(oauthProvider);
        app.use(new Router() {{
            get("/", new Middleware() {
                @Override
                public void handle(final YokeRequest request, final Handler<Object> next) {
                    request.loadSessionData(new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject session) {
                            if (session == null) {
                                request.response().end("home, logged in? false");
                            } else {
                                System.out.println(session.encodePrettily());
                                request.response().end("home, logged in? " + (session.getString("user") != null));
                            }
                        }
                    });
                }
            });

            get("/login", new Middleware() {
                @Override
                public void handle(final YokeRequest request, Handler<Object> next) {
                    request.loadSessionData(new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject session) {
                            if (session != null && session.getString("user") != null) {
                                request.response().redirect(303, "/");
                                return;
                            }

                            String next_url = request.getParameter("next", "/");
                            request.response().end("<html><form method=\"post\" action=\"/login\"><input type=\"hidden\" name=\"next\" value=\"" + next_url + "\"><input type=\"text\" placeholder=\"username\" name=\"username\"><input type=\"password\" placeholder=\"password\" name=\"password\"><button type=\"submit\">Login</button></form>");
                        }
                    });
                }
            });

            post("/login", new Middleware() {
                @Override
                public void handle(final YokeRequest request, final Handler<Object> next) {
                    JsonObject session = new JsonObject();
                    session.putString("user", request.getFormParameter("username"));

                    request.saveSessionData(session, new Handler<String>() {
                        @Override
                        public void handle(String status) {
                            if (!"ok".equals(status)) {
                                next.handle(status);
                                return;
                            }
                            request.response().redirect(303, request.getFormParameter("next", "/"));
                        }
                    });
                }
            });

            get("/logout", new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
                    request.destroySession();
                    request.response().redirect(303, "/");
                }
            });

            get("/protected_resource", new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
                        if (request.getParameter("access_token") != null) {
                            String accessToken = request.getParameter("access_token");
                            request.response().end(new JsonObject().putString("access_token", accessToken));
                        } else {
                            // no token found
                            next.handle(403);
                        }
                }
            });
        }});

        app.listen(8081);
    }
}