package com.jetdrone.vertx.yoke.extras.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.util.Utils;
import com.jetdrone.vertx.yoke.util.YokeAsyncResult;
import com.jetdrone.vertx.yoke.util.YokeException;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OAuth2 extends Middleware {

    private final SimpleDateFormat ISODATE;

    private Model model;
    private List<Allow> allow;
    private List<String> grants;
    private long accessTokenLifetime;
    private long refreshTokenLifetime;
    private long authCodeLifetime;

    private Pattern clientIdPattern;
    private Pattern grantTypePattern;

    // state

    private Allow allowCache;
    private Date now;
    private Authorise authorise;
    private Token token;

    // security objects
    private final SecureRandom crypto = new SecureRandom();
    private final MessageDigest hash;

    private static String join(List<?> list, String sep) {
        StringBuilder sb = new StringBuilder();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            sb.append(list.get(i));
            if (i+1 != size) {
                sb.append(sep);
            }
        }

        return sb.toString();
    }

    private Date parseISODate(String field) {
        if (field == null) {
            return null;
        }

        try {
            return ISODATE.parse(field);
        } catch (ParseException e) {
            return null;
        }
    }

    // TODO: request.put(user,...) should be consistent
    public OAuth2(Model model) {
        this(model, 3600, 1209600, 30, Pattern.compile("^[a-z0-9-_]{3,40}$", Pattern.CASE_INSENSITIVE));
    }

    public OAuth2(Model model, long accessTokenLifetime, long refreshTokenLifetime, long authCodeLifetime, Pattern clientIdPattern) {

        ISODATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS zzz");
        ISODATE.setTimeZone(TimeZone.getTimeZone("UTC"));

        this.allow = new ArrayList<>();
        this.grants = new ArrayList<>();

        this.model = model;

        this.accessTokenLifetime = accessTokenLifetime;
        this.refreshTokenLifetime = refreshTokenLifetime;
        this.authCodeLifetime = authCodeLifetime;

        this.clientIdPattern = clientIdPattern;
        this.grantTypePattern = Pattern.compile("^(" + join(this.grants, "|") + ")$", Pattern.CASE_INSENSITIVE);

        // state
        this.allowCache = null;

        this.authorise = new Authorise();
        this.token = new Token();

        try {
            this.hash = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static class Credentials {
        Credentials(String client_id, String client_secret) {
            this.client_id = client_id;
            this.client_secret = client_secret;
        }

        String client_id;
        String client_secret;
    }

    // TODO: this should be a JsonObject
    public static class OAuth {
        JsonObject token;
        JsonObject accessToken;
        String grantType;
        JsonObject client;
    }

    public interface Model {
        void getAccessToken(String bearerToken, AsyncResultHandler<JsonObject> handler);
        void getClient(String client_id, String client_secret, AsyncResultHandler<JsonObject> handler);
        void grantTypeAllowed(String client_id, String grant_type, AsyncResultHandler<Boolean> handler);
        void getUser(String username, String password, AsyncResultHandler<JsonObject> handler);
        void getRefreshToken(String refresh_token, AsyncResultHandler<JsonObject> handler);
        void revokeRefreshToken(String refresh_token, AsyncResultHandler<Void> handler);
        void saveAccessToken(String access_token, String client_id, String user_id, String expires, AsyncResultHandler<Void> handler);
        void saveRefreshToken(String refresh_token, String client_id, String user_id, String expires, AsyncResultHandler<Void> handler);
    }

    private static class Allow {
        int len;
        Pattern pattern;
    }


    private class Authorise {

        void handle(final YokeRequest request, final Handler<Object> next) {
            // Get token
            getBearerToken(request, new AsyncResultHandler<String>() {
                @Override
                public void handle(AsyncResult<String> getBearerToken) {
                    if (getBearerToken.failed()) {
                        next.handle(getBearerToken.cause());
                        return;
                    }

                    OAuth2.this.model.getAccessToken(getBearerToken.result(), new AsyncResultHandler<JsonObject>() {
                        @Override
                        public void handle(AsyncResult<JsonObject> getAccessToken) {
                            if (getAccessToken.failed()) {
                                next.handle(new YokeException(503, "server_error", getAccessToken.cause()));
                                return;
                            }

                            authorise.validateAccessToken(getAccessToken.result(), request, next);
                        }
                    });
                }
            });
        }

        void validateAccessToken(JsonObject token, YokeRequest request, Handler<Object> next) {
            if (token == null) {
                next.handle(new YokeException(400, "invalid_grant", "The access token provided is invalid."));
                return;
            }

            // Check it's valid
            Date expires = parseISODate(token.getString("expires"));
            if (expires == null || expires.before(OAuth2.this.now)) {
                next.handle(new YokeException(400, "invalid_grant", "The access token provided has expired."));
                return;
            }

            // Expose params
            OAuth oauth = request.get("oauth");
            oauth.token = token;
            request.put("user", token.getString("user_id"));

            next.handle(null); // Exit point
        }

        /**
         * Extract access token from request
         *
         * Checks exactly one access token had been passed and
         * does additional validation for each method of passing
         * the token.
         * Returns OAuth2 Error if any of the above conditions
         * aren't met.
         */
        void getBearerToken(YokeRequest request, AsyncResultHandler<String> next) {

            String headerToken = request.getHeader("Authorization");
            String getToken =  request.getParameter("access_token");
            String postToken = request.getFormParameter("access_token");

            // Check exactly one method was used
            int methodsUsed = (headerToken != null ? 1 : 0) + (getToken != null ? 1 : 0) + (postToken != null ? 1 : 0);

            if (methodsUsed > 1) {
                next.handle(new YokeAsyncResult<String>(new YokeException(400, "invalid_request", "Only one method may be used to authenticate at a time (Auth header, GET or POST).")));
                return;
            } else if (methodsUsed == 0) {
                next.handle(new YokeAsyncResult<String>(new YokeException(400, "invalid_request", "The access token was not found")));
                return;
            }

            // Header: http://tools.ietf.org/html/rfc6750#section-2.1
            if (headerToken != null) {
                Pattern p = Pattern.compile("Bearer\\s(\\S+)");
                Matcher matcher = p.matcher(headerToken);

                if (!matcher.matches()) {
                    next.handle(new YokeAsyncResult<String>(new YokeException(400, "invalid_request", "Malformed auth header")));
                    return;
                }

                headerToken = matcher.group(1);
                next.handle(new YokeAsyncResult<>(null, headerToken));
                return;
            }

            // POST: http://tools.ietf.org/html/rfc6750#section-2.2
            if (postToken != null) {
                if ("GET".equals(request.method())) {
                    next.handle(new YokeAsyncResult<String>(new YokeException(400, "invalid_request", "Method cannot be GET When putting the token in the body.")));
                    return;
                }

                if (!request.is("application/x-www-form-urlencoded")) {
                    next.handle(new YokeAsyncResult<String>(new YokeException(400, "invalid_request", "When putting the token in the body, content type must be application/x-www-form-urlencoded.")));
                    return;
                }

                next.handle(new YokeAsyncResult<>(null, postToken));
                return;
            }


            next.handle(new YokeAsyncResult<>(null, getToken));
        }
    }

    private class Token {
        public void handle(final YokeRequest request, final Handler<Object> next) {
            // Only POST via application/x-www-form-urlencoded is acceptable
            if (!"POST".equals(request.method()) || !request.is("application/x-www-form-urlencoded")) {
                next.handle(new YokeException(400, "invalid_request",
                        "Method must be POST with application/x-www-form-urlencoded encoding"));
                return;
            }

            // Grant type
            final OAuth o = request.get("oauth");
            o.grantType = request.getFormParameter("grant_type");
            if (o.grantType == null || !grantTypePattern.matcher(o.grantType).matches()) {
                next.handle(new YokeException(400, "invalid_request", "Invalid or missing grant_type parameter"));
                return;
            }

            // Extract credentials
            // http://tools.ietf.org/html/rfc6749#section-3.2.1
            Credentials creds = token.getClientCredentials(request);
            if (creds.client_id == null || !OAuth2.this.clientIdPattern.matcher(creds.client_id).matches()) {
                next.handle(new YokeException(400, "invalid_client", "Invalid or missing client_id parameter"));
                return;
            } else if (creds.client_secret == null) {
                next.handle(new YokeException(400, "invalid_client", "Missing client_secret parameter"));
                return;
            }

            // Check credentials against model
            OAuth2.this.model.getClient(creds.client_id, creds.client_secret, new AsyncResultHandler<JsonObject>() {
                @Override
                public void handle(AsyncResult<JsonObject> getClient) {
                    if (getClient.failed()) {
                        next.handle(new YokeException(503, "server_error", getClient.cause()));
                        return;
                    }

                    JsonObject client = getClient.result();

                    if (client == null) {
                        next.handle(new YokeException(400, "invalid_client", "The client credentials are invalid"));
                        return;
                    }

                    o.client = client;

                    OAuth2.this.model.grantTypeAllowed(client.getString("client_id"), o.grantType, new AsyncResultHandler<Boolean>() {
                        @Override
                        public void handle(AsyncResult<Boolean> grantTypeAllowed) {
                            if (grantTypeAllowed.failed()) {
                                next.handle(new YokeException(503, "server_error", grantTypeAllowed.cause()));
                                return;
                            }

                            Boolean allowed = grantTypeAllowed.result();

                            if (allowed == null || !allowed) {
                                next.handle(new YokeException(400, "invalid_client", "The grant type is unauthorised for this client_id"));
                                return;
                            }

                            token.grant(request, next);
                        }
                    });
                }
            });
        }

        private Credentials getClientCredentials(final YokeRequest request) {
            // Default return
            Credentials creds = new Credentials(request.getFormParameter("client_id"), request.getFormParameter("client_secret"));

            // Check for Basic Auth
            String authorization = request.getHeader("Authorization");
            if (authorization == null) return creds;

            String[] parts = authorization.split(" ");

            if (parts.length != 2) {
                return creds;
            }

            String scheme = parts[0];
            String credentials = new String(DatatypeConverter.parseBase64Binary(parts[1])).replace("^\\s+|\\s+$", "");
            int index = credentials.indexOf(":");

            if (!"Basic".equals(scheme) || index < 0) {
                return creds;
            }

            return new Credentials(credentials.substring(0, index), credentials.substring(index + 1));
        }

        private void grant(final YokeRequest request, final Handler<Object> next) {

            final OAuth o = request.get("oauth");

            switch (o.grantType) {
                case "password":
                    // User credentials
                    String uname = request.getFormParameter("username");
                    String pword = request.getFormParameter("password");

                    if (uname == null || pword == null) {
                        next.handle(new YokeException(400, "invalid_client","Missing parameters. \"username\" and \"password\" are required"));
                        return;
                    }

                    OAuth2.this.model.getUser(uname, pword, new AsyncResultHandler<JsonObject>() {
                        @Override
                        public void handle(AsyncResult<JsonObject> getUser) {
                            if (getUser.failed()) {
                                next.handle(new YokeException(503, "server_error", getUser.cause()));
                                return;
                            }

                            JsonObject user = getUser.result();

                            if (user != null) {
                                request.put("user", user);
                                grantAccessToken(request, next);
                            } else {
                                next.handle(new YokeException(400, "invalid_grant", "User credentials are invalid"));
                            }
                        }
                    });
                    return;
                case "refresh_token":
                    final String refresh_token = request.getFormParameter("refresh_token");

                    if (refresh_token == null) {
                        next.handle(new YokeException(400, "invalid_request", "No \"refresh_token\" parameter"));
                        return;
                    }

                    OAuth2.this.model.getRefreshToken(refresh_token, new AsyncResultHandler<JsonObject>() {
                        @Override
                        public void handle(AsyncResult<JsonObject> getRefreshToken) {
                            if (getRefreshToken.failed()) {
                                next.handle(new YokeException(503, "server_error", getRefreshToken.cause()));
                                return;
                            }

                            JsonObject refreshToken = getRefreshToken.result();

                            if (refreshToken == null || !refreshToken.getString("client_id").equals(o.client.getString("client_id"))) {
                                next.handle(new YokeException(400, "invalid_grant", "Invalid refresh token"));
                                return;
                            } else {
                                Date expires = parseISODate(refreshToken.getString("expires"));
                                if (expires != null && expires.before(OAuth2.this.now)) {
                                    next.handle(new YokeException(400, "invalid_grant", "Refresh token has expired"));
                                    return;
                                }
                            }

                            if (refreshToken.getString("user_id") != null) {
                                request.put("user", refreshToken.getString("user_id"));
                            } else {
                                next.handle(new YokeException(503, "server_error", "No user/user_id parameter returned from getRefreshToken"));
                                return;
                            }

                            OAuth2.this.model.revokeRefreshToken(refresh_token, new AsyncResultHandler<Void>() {
                                @Override
                                public void handle(AsyncResult<Void> revokeRefreshToken) {
                                    if (revokeRefreshToken.failed()) {
                                        next.handle(new YokeException(503, "server_error", revokeRefreshToken.cause()));
                                        return;
                                    }

                                    grantAccessToken(request, next);
                                }
                            });
                        }
                    });
                    return;

                default:
                    next.handle(new YokeException(400, "invalid_request", "Invalid grant_type parameter or parameter missing"));
            }
        }

        private void grantAccessToken(final YokeRequest request, final Handler<Object> next) {
            final OAuth o = request.get("oauth");
            final String accessToken = generateToken("accessToken");

            o.accessToken.putString("access_token", accessToken);

            Date expires = null;
            if (OAuth2.this.accessTokenLifetime != 0) {
                expires = new Date(OAuth2.this.now.getTime() + OAuth2.this.accessTokenLifetime * 1000);
            }

            OAuth2.this.model.saveAccessToken(accessToken, o.client.getString("client_id"), (String) request.get("user"), ISODATE.format(expires), new AsyncResultHandler<Void>() {
                @Override
                public void handle(AsyncResult<Void> saveAccessToken) {
                    if (saveAccessToken.failed()) {
                        next.handle(new YokeException(503, "server_error", saveAccessToken.cause()));
                        return;
                    }

                    // Are we issuing refresh tokens?
                    if (OAuth2.this.grants.indexOf("refresh_token") >= 0) {
                        o.accessToken.putString("refresh_token", generateToken("refreshToken"));

                        Date expires = null;
                        if (OAuth2.this.refreshTokenLifetime != 0) {
                            expires = new Date(OAuth2.this.now.getTime() + OAuth2.this.refreshTokenLifetime);
                        }

                        OAuth2.this.model.saveRefreshToken(o.accessToken.getString("refresh_token"), o.client.getString("client_id"), (String) request.get("user"), ISODATE.format(expires), new AsyncResultHandler<Void>() {
                            @Override
                            public void handle(AsyncResult<Void> saveRefreshToken) {
                                if (saveRefreshToken.failed()) {
                                    next.handle(new YokeException(503, "server_error", saveRefreshToken.cause()));
                                    return;
                                }

                                issueToken(request, next);
                            }
                        });
                    } else {
                        issueToken(request, next);
                    }
                }
            });
        }

        private void issueToken(final YokeRequest request, final Handler<Object> next) {
            final OAuth o = request.get("oauth");
            // Prepare for output
            o.accessToken.putString("token_type", "bearer");
            if (OAuth2.this.accessTokenLifetime != 0) {
                o.accessToken.putNumber("expires_in", OAuth2.this.accessTokenLifetime);
            }

            // That's it!
            request.response().putHeader("Cache-Control", "no-cache");
            request.response().putHeader("Pragme", "no-cache");
            request.response().end(o.accessToken);
        }

        private String generateToken(String type) {
            byte[] data = new byte[256];
            crypto.nextBytes(data);

            hash.reset();
            return Utils.hex(hash.digest(data));
        }
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        Allow allow = allowCache;

        // Build allow object this method if haven't yet already
        if (allow == null) {
            allow = new Allow();
            allow.len = this.allow.size();
            allow.pattern = Pattern.compile("^(" + join(this.allow, "|") + ")$");

            allowCache = allow;
        }

        // Setup request params
        OAuth o = request.get("oauth");
        request.put("oauth", o);
        this.now = new Date();

        if ("/oauth/token".equals(request.path())) {
            token.handle(request, next);
        } else if (allow.len == 0 || !allow.pattern.matcher(request.path()).matches()) {
            authorise.handle(request, next);
        } else {
            next.handle(null);
        }
    }

    /**
     * Error Handler
     *
     * Provides OAuth error handling middleware to catch any errors
     * and ensure an oauth complient response
     *
     * @return {Function} OAuth error handling middleware
     */
    void errorHandler() {
//        var oauth = this;
//
//        return function (err, req, res, next) {
//            if (err instanceof Error && err.status && err.status === 400) {
//                err = error('invalid_request', err.toString(), err);
//            } else if (!(err instanceof error)) {
//                err = error('server_error', false, err);
//            }
//
//            if (oauth.debug) console.log(err.stack || err);
//            if (oauth.passthroughErrors && !req.oauth.internal) return next(err);
//
//            delete err.stack;
//            res.send(err.code, err);
//        };
    }
}
