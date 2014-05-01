package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.core.YokeException;
import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.Handler;

import java.util.regex.Pattern;

public class JWT extends Middleware {

    private static final Pattern BEARER = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);

    private final String skip;
    private final com.jetdrone.vertx.yoke.util.JWT jwt;

    public JWT(@NotNull String secret) {
        this(secret, null);
    }

    public JWT(@NotNull String secret, String skip) {
        this.skip = skip;
        this.jwt = new com.jetdrone.vertx.yoke.util.JWT(secret);
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
            request.put("jwt", jwt.decode(token));
            next.handle(null);
        } catch (RuntimeException e) {
            next.handle(new YokeException(401, e));
        }
    }
}
