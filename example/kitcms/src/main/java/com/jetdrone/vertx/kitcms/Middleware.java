package com.jetdrone.vertx.kitcms;

import com.jetdrone.vertx.kit.BaseVerticle;
import com.jetdrone.vertx.kit.KitRequest;
import org.vertx.java.core.Handler;

public class Middleware {

    final Config config;
    final BaseVerticle verticle;

    public Middleware(BaseVerticle verticle, Config config) throws Exception {
        this.verticle = verticle;
        this.config = config;
    }

    // Middleware
    public void domain(final KitRequest kit, final Handler<Void> handler) {
        String host = kit.headers.get("host");
        if (host == null) {
            kit.context.put("message", "There is no host header");
            verticle.error(kit, 400);
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
                kit.context.put("message", "There is no host header");
                verticle.error(kit, 404);
            } else {
                kit.context.put("domain", found);
                handler.handle(null);
            }
        }
    }
}
