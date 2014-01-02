package com.jetdrone.vertx.yoke.extras.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.util.Utils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class OAuth2Provider extends Middleware {

    interface OAuth2Store {
        void lookupGrant(String clientId, String clientSecret, String code, Handler<String> handler);
        void createAccessToken(String userId, String clientId, Handler<String> handler);
    }

    private Key cryptSecret;
    private OAuth2Store store;

    public OAuth2Provider(String signKey) {
        cryptSecret = new SecretKeySpec(signKey.getBytes(), "AES");
    }

    private String encodeUrlSaveBase64(String str) {
        return str.replaceAll("\\+", "-").replaceAll("/", "_").replaceAll("=+$", "");
    }

    private String decodeUrlSaveBase64(String str) {
        str = (str + "===").substring(0, str.length() + (str.length() % 4));
        return str.replaceAll("-", "+").replaceAll("_", "/");
    }

    private String encrypt(String data) {
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, cryptSecret);
            return encodeUrlSaveBase64(Utils.base64(c.doFinal(data.getBytes())));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private String decrypt(String data) {
        String str = decodeUrlSaveBase64(data);

        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, cryptSecret);
            return new String(c.doFinal(str.getBytes()));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
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

    private void postAccessToken(final YokeRequest request, Handler<Object> next) {
        final String clientId = request.formAttributes().get("client_id");
        final String clientSecret = request.formAttributes().get("client_secret");
        final String redirectUri = request.formAttributes().get("redirect_uri");
        String code = request.formAttributes().get("code");

        try {
            code = decrypt(code);
        } catch (RuntimeException e) {
            // TODO: proper error
            next.handle("accessDenied");
            return;
        }

        store.lookupGrant(clientId, clientSecret, code, new Handler<String>() {
            @Override
            public void handle(String userId) {
                // TODO: if null?
                store.createAccessToken(userId, clientId, new Handler<String>() {
                    @Override
                    public void handle(String tokenDataStr) {
                        // TODO: if null?
                        String atok = encrypt(tokenDataStr);
                        request.response().end(new JsonObject().putString("access_token", atok));
                    }
                });
            }
        });
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