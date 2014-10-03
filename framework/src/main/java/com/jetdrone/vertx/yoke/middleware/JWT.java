package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.AbstractMiddleware;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.core.YokeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import java.util.regex.Pattern;

public class JWT extends AbstractMiddleware {

    private static final Pattern BEARER = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);

    public interface JWTHandler {
        public void handle(JsonObject token, Handler<Object> result);
    }

    private final String skip;

    private com.jetdrone.vertx.yoke.security.JWT jwt;

    private final JWTHandler handler;

    public JWT() {
        this(null);
    }

    public JWT(final @Nullable String skip) {
        this(skip, null);
    }

    public JWT(final @Nullable String skip, final @Nullable JWTHandler handler) {
        this.skip = skip;
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

        if ("OPTIONS".equals(request.method()) && request.getHeader("access-control-request-headers") != null) {
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

            final long now = System.currentTimeMillis();

            if (jwtToken.containsField("iat")) {
                Long iat = jwtToken.getLong("iat");
                // issue at must be in the past
                if (iat >= now) {
                    next.handle(new YokeException(401, "Invalid Token!"));
                    return;
                }
            }

            if (jwtToken.containsField("nbf")) {
                Long nbf = jwtToken.getLong("nbf");
                // not before must be after now
                if (nbf >= now) {
                    next.handle(new YokeException(401, "Invalid Token!"));
                    return;
                }
            }

            if (jwtToken.containsField("exp")) {
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
