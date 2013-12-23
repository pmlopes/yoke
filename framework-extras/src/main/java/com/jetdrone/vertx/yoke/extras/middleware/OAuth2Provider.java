package com.jetdrone.vertx.yoke.extras.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.vertx.java.core.Handler;

public class OAuth2Provider extends Middleware {

    private String cryptSecret;

    public OAuth2Provider(String signKey) {
        cryptSecret = signKey;
    }

    private String encodeUrlSaveBase64(String str) {
        // return str.replace(/\+/g, '-').replace(/\//g, '_').replace(/\=+$/, '');

        return null;
    }

    private String decodeUrlSaveBase64(String str) {
        // str = (str + '===').slice(0, str.length + (str.length % 4));
        //return str.replace(/-/g, '+').replace(/_/g, '/');
        return null;
    }

    private String encrypt(String data) {
//        var cipher = crypto.createCipher("aes256", this.cryptSecret);
//        var str = cipher.update(data, 'utf8', 'base64') + cipher.final('base64');
//        str = this._encodeUrlSaveBase64(str);
//        return str;
        return null;
    }

    private String decrypt(String data) {
//        var str = this._decodeUrlSaveBase64(data);
//        var decipher = crypto.createDecipher("aes256", this.cryptSecret);
//        str = decipher.update(str, 'base64', 'utf8') + decipher.final('base64');
//        return str;
        return null;
    }

    public void validateToken(String token, Object callback) {
//        var self = this,
//                tokenData;
//
//        try {
//                tokenData = self._decrypt(token);
//        } catch(e) {
//                return callback(new Error("decrypting token failed"));
//        }
//
//        callback(null, tokenData);
    }

    private void getOauth(YokeRequest request, Handler<Object> next) {
//        var self = this,
//                clientId = req.query.client_id,
//                redirectUri = req.query.redirect_uri,
//                responseType = req.query.response_type || 'token';
//
//        if(!clientId || !redirectUri) {
//                return self.emit('authorizeParamMissing', req, res, next);
//        }
//
//        var authorizeUrl = req.url;
//
//        self.emit('enforceLogin', req, res, authorizeUrl, function(userId) {
//                self.emit('shouldSkipAllow', userId, clientId, function(skip, tokenDataStr) {
//                        if(skip) {
//                                self._validateThings(req, res, clientId, redirectUri, responseType, function(){
//                                        if(tokenDataStr) {
//                                                self._redirectWithToken(tokenDataStr, redirectUri, res);
//                                        }else{
//                                                self.emit('createAccessToken', userId, clientId, function(tokenDataStr) {
//                                                        self._redirectWithToken(tokenDataStr, redirectUri, res);
//                                                });
//                                        }
//                                });
//                        }else{
//                                authorizeUrl += '&x_user_id=' + self._encrypt(userId);
//                                self.emit('authorizeForm', req, res, clientId, authorizeUrl);
//                        }
//                });
//        });
    }

    private void postOauth(YokeRequest request, Handler<Object> next) {
//        var self = this;
//
//        var clientId = req.query.client_id,
//                url = req.query.redirect_uri,
//                responseType = req.query.response_type || 'token',
//                state = req.query.state,
//                xUserId = req.query.x_user_id;
//
//        self._validateThings(req, res, clientId, url, responseType, function(){
//
//                if(!req.body.allow) {
//                        return self._redirectError(res, responseType, url, "access_denied");
//                }
//
//                if('token' === responseType) {
//                        var userId;
//                        try {
//                                userId = self._decrypt(xUserId);
//                        } catch(e) {
//                                return self.emit('parameterError', req, res);
//                        }
//
//                        self.emit('createAccessToken', userId, clientId, function(tokenDataStr) {
//                                var atok = self._encrypt(tokenDataStr);
//                                url += "#access_token=" + atok;
//                                res.writeHead(303, {Location: url});
//                                res.end();
//                        });
//                } else {
//                        self.emit('createGrant', req, clientId, function(codeStr) {
//                                codeStr = self._encrypt(codeStr);
//                                url += "?code=" + codeStr;
//
//                                // pass back anti-CSRF opaque value
//                                if(state) {
//                                        url += "&state=" + state;
//                                }
//
//                                res.writeHead(303, {Location: url});
//                                res.end();
//                        });
//                }
//        });
    }

    private void redirectWithToken(String tokenDataStr, String redirectUri, Object callback) {
//        var atok = this._encrypt(tokenDataStr);
//        redirectUri += "#access_token=" + atok;
//        res.writeHead(303, {Location: redirectUri});
//        return res.end();
    }

    private void validateThings(Object req, Object res, String clientId, String redirectUri, Object responseType, Object callback) {
//    var self = this;
//            if(responseType !== "code" && responseType !== "token") {
//                    return self.emit('responseTypeError', req, res);
//            }
//            self.emit('validateClientIdAndRedirectUri', clientId, redirectUri, req, res, callback);
    }

    private void redirectError(Object res, Object responseType, String url, Object error) {
//var sep = responseType === "token" ? "#" : "?";
//        res.writeHead(303, {Location: url + sep + "error=" + error});
//        return res.end();
    }

    private void postAccessToken(YokeRequest request, Handler<Object> next) {
//        var self = this,
//                clientId = req.body.client_id,
//                clientSecret = req.body.client_secret,
//                redirectUri = req.body.redirect_uri,
//                code = req.body.code;
//
//        try {
//                code = self._decrypt(code);
//        } catch(e) {
//                return self.emit('accessDenied', req, res);
//        }
//
//        self.emit('lookupGrant', clientId, clientSecret, code, res, function(userId) {
//                self.emit('createAccessToken', userId, clientId, function(tokenDataStr) {
//                        var atok = self._encrypt(tokenDataStr);
//                        res.json({access_token:atok});
//                });
//        });
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        if ("GET".equals(request.method()) && "/oauth/authorize".equals(request.path())) {
            getOauth(request, next);
        } else if ("POST".equals(request.method()) && "/oauth/authorize".equals(request.path())) {
            postOauth(request, next);
        } else if ("POST".equals(request.method()) && "/oauth/access_token".equals(request.path())) {
            postAccessToken(request, next);
        } else {
            next.handle(null);
        }
    }
}