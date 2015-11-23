package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.core.YokeException;
import io.vertx.core.http.HttpMethod;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.regex.Pattern;

public class JWT extends Middleware {

    private static final Pattern BEARER = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);

    public interface JWTHandler {
        public void handle(JsonObject token, Handler<Object> result);
    }

    private final String skip;

    private com.jetdrone.vertx.yoke.security.JWT jwt;

    private final JWTHandler handler;

    public JWT() {
        this.skip = null;
        this.handler = null;
    }

    public JWT(final @NotNull String skip) {
        this.skip = skip;
        this.handler = null;
    }

    public JWT(final @NotNull String skip, final @NotNull JWTHandler handler) {
        this.skip = skip;
        this.handler = handler;
    }

    public JWT(final @NotNull JWTHandler handler) {
        this.skip = null;
        this.handler = handler;
    }

    @Override
    public Middleware init(@NotNull final Yoke yoke, @NotNull final String mount) {
        super.init(yoke, mount);

        jwt = new com.jetdrone.vertx.yoke.security.JWT(yoke.security());
        return this;
    }

    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
        String token = null;

        if (HttpMethod.OPTIONS.equals(request.method()) && request.getHeader("access-control-request-headers") != null) {
            for (String ctrlReq : request.getHeader("access-control-request-headers").split(",")) {
                if (ctrlReq.contains("authorization")) {
                    next.handle(null);
                    return;
                }
            }
        }

        if (skip != null && skip.contains(request.normalizedPath())) {
            next.handle(null);
            return;
        }

        final String authorization = request.getHeader("authorization");

        if (authorization != null) {
            String[] parts = authorization.split(" ");
            if (parts.length == 2) {
                final String scheme = parts[0],
                             credentials = parts[1];

                if (BEARER.matcher(scheme).matches()) {
                    token = credentials;
                }
            } else {
                next.handle(new YokeException(401, "Format is Authorization: Bearer [token]"));
                return;
            }
        } else {
            next.handle(new YokeException(401, "No Authorization header was found"));
            return;
        }

        try {
            final JsonObject jwtToken = jwt.decode(token);

            // All dates in JWT are of type NumericDate
            // a NumericDate is: numeric value representing the number of seconds from 1970-01-01T00:00:00Z UTC until
            // the specified UTC date/time, ignoring leap seconds
            final long now = System.currentTimeMillis() / 1000;

            if (jwtToken.containsKey("iat")) {
                Long iat = jwtToken.getLong("iat");
                // issue at must be in the past
                if (iat > now) {
                    next.handle(new YokeException(401, "Invalid Token!"));
                    return;
                }
            }

            if (jwtToken.containsKey("nbf")) {
                Long nbf = jwtToken.getLong("nbf");
                // not before must be after now
                if (nbf > now) {
                    next.handle(new YokeException(401, "Invalid Token!"));
                    return;
                }
            }

            if (jwtToken.containsKey("exp")) {
                Long exp = jwtToken.getLong("exp");
                // expires must be after now
                if (now > exp) {
                    next.handle(new YokeException(401, "Invalid Token!"));
                    return;
                }
            }
            request.put("jwt", jwtToken);

            if (handler == null) {
                next.handle(null);
                return;
            }

            handler.handle(jwtToken, next);
        } catch (RuntimeException e) {
            next.handle(new YokeException(401, e));
        }
    }
}
