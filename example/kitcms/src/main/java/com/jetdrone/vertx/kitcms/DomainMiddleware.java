package com.jetdrone.vertx.kitcms;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.vertx.java.core.Handler;

public class DomainMiddleware extends Middleware {

    private final Config config;

    public DomainMiddleware(Config config) {
        this.config = config;
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        String host = request.getHeader("host");
        if (host == null) {
            // there is no host header
            next.handle(400);
        } else {
            if (host.indexOf(':') != -1) {
                host = host.substring(0, host.indexOf(':'));
            }

            Config.Domain found = null;

            for (Config.Domain domain : config.domains) {
                if (domain.pattern.matcher(host).find()) {
                    found = domain;
                    break;
                }
            }

            if (found == null) {
                // still no host found even with header present
                next.handle(404);
            } else {
                request.put("domain", found);
                next.handle(null);
            }
        }
    }
}
