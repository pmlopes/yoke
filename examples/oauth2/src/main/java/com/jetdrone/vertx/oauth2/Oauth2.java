package com.jetdrone.vertx.oauth2;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.extras.middleware.OAuth2Provider;
import com.jetdrone.vertx.yoke.middleware.BodyParser;
import com.jetdrone.vertx.yoke.middleware.CookieParser;
import org.vertx.java.platform.Verticle;

public class Oauth2 extends Verticle {

    @Override
    public void start() {

//var OAuth2Provider = require('../index'),
//	express = require('express'),
//	MemoryStore = express.session.MemoryStore;

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

        // app.use(express.logger());
        app.use(new BodyParser());
        app.use(new CookieParser());
//        app.use(express.session({store: new MemoryStore({reapInterval: 5 * 60 * 1000}), secret: 'abracadabra'}));
//        app.use(oauthProvider.oauth());
//
//app.get('/', function(req, res, next) {
//	console.dir(req.session);
//	res.end('home, logged in? ' + !!req.session.user);
//});
//
//app.get('/login', function(req, res, next) {
//	if(req.session.user) {
//		res.writeHead(303, {Location: '/'});
//		return res.end();
//	}
//
//	var next_url = req.query.next ? req.query.next : '/';
//	res.end('<html><form method="post" action="/login"><input type="hidden" name="next" value="' + next_url + '"><input type="text" placeholder="username" name="username"><input type="password" placeholder="password" name="password"><button type="submit">Login</button></form>');
//});
//
//app.post('/login', function(req, res, next) {
//	req.session.user = req.body.username;
//	res.writeHead(303, {Location: req.body.next || '/'});
//	res.end();
//});
//
//app.get('/logout', function(req, res, next) {
//	req.session.destroy(function(err) {
//		res.writeHead(303, {Location: '/'});
//		res.end();
//	});
//});
//
//app.get('/protected_resource', function(req, res, next) {
//	if(req.query.access_token) {
//		var accessToken = req.query.access_token;
//		res.json(accessToken);
//	} else {
//		res.writeHead(403);
//		res.end('no token found');
//	}
//});
//
//app.listen(8081);

    }
}
