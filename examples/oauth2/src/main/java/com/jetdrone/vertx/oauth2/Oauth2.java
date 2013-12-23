package com.jetdrone.vertx.oauth2;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.extras.middleware.OAuth2Provider;
import com.jetdrone.vertx.yoke.middleware.*;
import com.jetdrone.vertx.yoke.store.SessionStore;
import com.jetdrone.vertx.yoke.store.SharedDataSessionStore;
import com.jetdrone.vertx.yoke.util.Utils;
import org.vertx.java.core.Handler;
import org.vertx.java.platform.Verticle;

import javax.crypto.Mac;

public class Oauth2 extends Verticle {

    @Override
    public void start() {

        SessionStore sessionStore = new SharedDataSessionStore(vertx, "oauth2");
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

        app.use(new Logger());
        app.use(new BodyParser());
        app.use(new CookieParser());
        app.use(new Session(mac));
        app.use(oauthProvider);
        app.use(new Router() {{
            get("/", new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
//                        console.dir(req.session);
//                        res.end('home, logged in? ' + !!req.session.user);
                }
            });

            get("/login", new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
//                        if (req.session.user) {
//                            res.writeHead(303, {Location:'/'});
//                            return res.end();
//                        }
//
//                        var next_url = req.query.next ? req.query.next : '/';
//                        res.end('<html><form method="post" action="/login"><input type="hidden" name="next" value="' + next_url + '"><input type="text" placeholder="username" name="username"><input type="password" placeholder="password" name="password"><button type="submit">Login</button></form>');
                }
            });

            post("/login", new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
//                        req.session.user = req.body.username;
//                        res.writeHead(303, {Location:req.body.next || '/'});
//                        res.end();
                }
            });

            get("/logout", new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
//                        req.session.destroy(function(err) {
//                            res.writeHead(303, {Location:'/'});
//                            res.end();
//                        });
                }
            });

            get("/protected_resource", new Middleware() {
                @Override
                public void handle(YokeRequest request, Handler<Object> next) {
//                        if (req.query.access_token) {
//                            var accessToken = req.query.access_token;
//                            res.json(accessToken);
//                        } else {
//                            res.writeHead(403);
//                            res.end('no token found');
//                        }
                }
            });
        }});

        app.listen(8081);
    }
}