package com.jetdrone.vertx.yoke.extras.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.security.YokeSecurity;
import com.jetdrone.vertx.yoke.core.YokeException;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

public abstract class OAuth2 {

    private final String authorize_uri = "/oauth/authorize";
    private final String access_token_uri = "/oauth/access_token";

    private final Key cryptoKey = YokeSecurity.newKey("secret", "AES");
    private final Cipher encCipher = YokeSecurity.newCipher(cryptoKey, Cipher.ENCRYPT_MODE);
    private final Cipher decCipher = YokeSecurity.newCipher(cryptoKey, Cipher.DECRYPT_MODE);
    private final Mac signMac = YokeSecurity.newMac("HmacSHA256", "sign");

    public OAuth2() {

    }

    private JsonObject parseAuthorization(String authorization) {
        if (authorization == null) {
            return null;
        }

        final JsonObject response = new JsonObject();

        final String user;
        final String pass;
        final String scheme;

        try {
            String[] parts = authorization.split(" ");
            if (parts.length != 2) {
                return null;
            }

            scheme = parts[0];

            if (!"Basic".equals(scheme)) {
                return null;
            }

            String[] credentials = new String(DatatypeConverter.parseBase64Binary(parts[1])).split(":");
            user = credentials[0];
            pass = credentials[1];

            response.putString("username", user).putString("password", pass);

        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }

        return response;
    }

    private String stringify(JsonElement json) {
        // TODO: encrypt + sign
        if (json.isArray()) {
            return ((JsonArray) json).encode();
        } else if (json.isObject()) {
            return ((JsonObject) json).encode();
        }
        return null;
    }

    private String urify(JsonObject json) {
        // TODO: convert json object to uri param encoded string
        return null;
    }

    private JsonElement parse(String str) {
        // TODO: decrypt + verify sign
        if (str.charAt(0) == '{') {
            return new JsonObject(str);
        }
        if (str.charAt(0) == '[') {
            return new JsonArray(str);
        }
        return null;
    }

    private JsonObject generateAccessToken(String user_id, String client_id, String extra_data, JsonObject token_options) {
        JsonObject out = new JsonObject()
                .putString("access_token", stringify(new JsonArray().add(user_id).add(client_id).add(System.currentTimeMillis()).add(extra_data)))
                .putString("refresh_token", null);

        if (token_options != null) {
            out.mergeIn(token_options);
        }

        return out;
    }

    public final Middleware login = new Middleware() {
        @Override
        public void handle(YokeRequest request, Handler<Object> next) {
            String atok, user_id, client_id, extra_data;
            Date grant_date;

            if(request.getParameter("access_token") != null) {
                atok = request.getParameter("access_token");
            } else if(request.getParameter("authorization", "").indexOf("Bearer ") == 0) {
                atok = request.getParameter("authorization").replace("Bearer", "").trim();
            } else {
                next.handle(null);
                return;
            }

            try {
                JsonArray data = parse(atok).asArray();
                user_id = data.get(0);
                client_id = data.get(1);
                grant_date = new Date((Long) data.get(2));
                extra_data = data.get(3);
            } catch(RuntimeException e) {
                next.handle(new YokeException(400, e));
                return;
            }

            access_token(request, new JsonObject()
                    .putString("user_id", user_id)
                    .putString("client_id", client_id)
                    .putString("extra_data", extra_data)
                    // TODO: this should be a date
                    .putNumber("grant_date", grant_date.getTime()), next);
        }
    };

    public final Middleware oauth = new Middleware() {
        @Override
        public void handle(final YokeRequest request, final Handler<Object> next) {
            // TODO: remove prefix
            String uri = request.path();

            if("GET".equals(request.method()) && authorize_uri.equals(uri)) {
                final String client_id = request.getParameter("client_id");
                final String redirect_uri = request.getFormParameter("redirect_uri");

                if(client_id == null || redirect_uri == null) {
                    next.handle(new YokeException(400, "client_id and redirect_uri required"));
                    return;
                }

                // authorization form will be POSTed to same URL, so we"ll have all params
                enforce_login(request, request.uri(), new Handler<String>() {
                    @Override
                    public void handle(String user_id) {
                        // store user_id in an HMAC-protected encrypted query param
                        String authorize_url = request.uri() + "&" + "x_user_id=" + YokeSecurity.sign(YokeSecurity.encrypt(user_id, encCipher), signMac);

                        // user is logged in, render approval page
                        authorize_form(request, client_id, authorize_url);
                    }
                });

            } else if("POST".equals(request.method())  && authorize_uri.equals(uri)) {
                final String client_id = request.getParameter("client_id", request.getFormParameter("client_id"));
                final String redirect_uri = request.getParameter("redirect_uri", request.getFormParameter("redirect_uri"));
                final String response_type = request.getParameter("response_type", request.getFormParameter("response_type", "code"));
                final String state = request.getParameter("state", request.getFormParameter("state"));
                final String x_user_id = request.getParameter("x_user_id", request.getFormParameter("x_user_id"));

                final String url;

                switch(response_type) {
                    case "code": url = redirect_uri + "?"; break;
                    case "token": url = redirect_uri + "#"; break;
                    default:
                        next.handle(new YokeException(400, "invalid response_type requested"));
                        return;
                }

                // TODO: use better API once available
                JsonObject json = request.body();
                if(json.toMap().containsKey("allow")) {
                    if("token".equals(response_type)) {
                        final String user_id;

                        try {
                            user_id = YokeSecurity.unsign(YokeSecurity.decrypt(x_user_id, decCipher), signMac);
                        } catch(RuntimeException e) {
                            next.handle(e);
                            return;
                        }

                        create_access_token(user_id, client_id, new Handler<JsonObject>() {
                            @Override
                            public void handle(JsonObject json) {
                                final JsonObject atok = generateAccessToken(user_id, client_id, json.getString("extra_data"), json.getObject("token_options"));

                                save_access_token(user_id, client_id, atok, new Handler<Object>() {
                                    @Override
                                    public void handle(Object event) {

                                        request.response().redirect(303, url + urify(atok));
                                    }
                                });
                            }
                        });
                    } else {
                        final String code = UUID.randomUUID().toString();

                        save_grant(request, client_id, code, new Handler<Object>() {
                            @Override
                            public void handle(Object error) {
                                JsonObject extras = new JsonObject().putString("code", code);

                                // pass back anti-CSRF opaque value
                                if(state != null)
                                    extras.putString("state", state);

                                request.response().redirect(303, url + urify(extras));
                            }
                        });
                    }
                } else {
                    request.response().redirect(303, url + "&error=access_denied");
                }

            } else if("POST".equals(request.method())  && access_token_uri.equals(uri)) {
                String _client_id = request.getFormParameter("client_id");
                String client_secret = request.getFormParameter("client_secret");
                String redirect_uri = request.getFormParameter("redirect_uri");
                final String code = request.getFormParameter("code");

                if(_client_id == null || client_secret == null) {
                    JsonObject authorization = parseAuthorization(request.getHeader("authorization"));

                    if(authorization == null) {
                        next.handle(new YokeException("client_id and client_secret required"));
                        return;
                    }

                    _client_id = authorization.getString("username");
                    client_secret = authorization.getString("password");
                }

                final String client_id = _client_id;

                if("password".equals(request.getFormParameter("grant_type"))) {
                    client_auth(client_id, client_secret, request.getFormParameter("username"), request.getFormParameter("password"), new Handler<AsyncResult<String>>() {
                        @Override
                        public void handle(AsyncResult<String> result) {

                            if(result.failed()) {
                                next.handle(new YokeException(401, result.cause()));
                                return;
                            }

                            createAccessToken(result.result(), client_id, new Handler<JsonObject>() {
                                @Override
                                public void handle(JsonObject atok) {
                                    request.response().end(atok);
                                }
                            });
                        }
                    });
                } else {
                    lookup_grant(client_id, client_secret, code, new Handler<AsyncResult<String>>() {
                        @Override
                        public void handle(final AsyncResult<String> result) {
                            if(result.failed()) {
                                next.handle(new YokeException(400, result.cause()));
                                return;
                            }

                            createAccessToken(result.result(), client_id, new Handler<JsonObject>() {
                                @Override
                                public void handle(final JsonObject atok) {

                                    remove_grant(result.result(), client_id, code, new Handler<Object>() {
                                        @Override
                                        public void handle(Object err) {
                                            request.response().end(atok);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }

            } else {
                next.handle(null);
            }
        }
    };

    private void createAccessToken(final String user_id, final String client_id, final Handler<JsonObject> next) {
        create_access_token(user_id, client_id, new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject json) {
                final JsonObject atok = generateAccessToken(user_id, client_id, json.getString("extra_data"), json.getObject("token_options"));

                save_access_token(user_id, client_id, atok, new Handler<Object>() {
                    @Override
                    public void handle(Object event) {
                        next.handle(atok);
                    }
                });
            }
        });
    }

    // Store interaction

    private void access_token(Object... args) {
//        function(req,token,next){
//            console.log("Access token request",token);
//
//            loadUserById(token.user_id, function(err,user){
//                if(user){
//                    req.session.user=user;
//                    next();
//                } else {
//                    next(new Error("Unable to load user"));
//                }
//            }
//        }
    }

    private void create_access_token(Object... args) {
        // User defined or:

//        function(user_id,client_id,next){ return next(null);
    }

    private void save_access_token(Object... args) {
// Optional???
    }

    private void enforce_login(Object... args) {
//        function(req,res,authorize_url,next){
//            if(req.session.user){
//                next(req.session.user);
//            } else {
//                res.writeHead(303,{ Location: Hop._OAuth.options.loginURL+"?next="+encodeURIComponent(authorize_url)});
//                res.end();
//            }
//        }
    }

    private void authorize_form(Object... args) {
//        function(req,res,client_id,authorize_url){
//            if(options.loginTemplate){
//                res.render(Hop._OAuth.options.loginTemplate,{ client_id: client_id, authorize_url:authorize_url});
//            } else {
//                res.end('<html>this app wants to access your account... <form method="post" action="' + authorize_url + '"><button name="allow">Allow</button><button name="deny">Deny</button></form>');
//            }
//        }
    }

    private void client_auth(Object... args) {

    }

    private void lookup_grant(Object... args) {
//        this.lookupGrant=function(client_id,client_secret,code,next){
//            //console.log("lookup grant",client_id,client_secret,code);
//            var key = "oauth_"+client_id.toString()+"_"+code.toString();
//            options.redisClient.get(key,function(err,data){
//                if(data!=null){
//                    var user = JSON.parse(data.toString());
//                    next(null,user);
//                } else {
//                    next(new Error("Invalid grant"));
//                }
//            });
//        }
    }

    private void remove_grant(Object... args) {
//        this.removeGrant=function(user_id,client_id,code){
//            //console.log("remove grant",user_id,client_id,code);
//            var key = "oauth_"+client_id.toString()+"_"+code.toString();
//            options.redisClient.del(key);
//        }
    }

    private void save_grant(Object... args) {
//        this.saveGrant=function(req,client_id,code,next){
//            //console.log("Save grant",null,client_id,code);
//            var key = "oauth_"+client_id.toString()+"_"+code.toString();
//            options.redisClient.set(key,options.getUserId(req.session.user));
//            next();
//        }
    }
}
