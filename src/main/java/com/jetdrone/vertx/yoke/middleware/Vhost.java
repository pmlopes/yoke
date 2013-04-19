package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.Map;
import java.util.regex.Pattern;

public class Vhost extends Middleware {

    private final Handler<HttpServerRequest> handler;
    private final Pattern regex;

    public Vhost(String hostname, Handler<HttpServerRequest> handler) {
        this.handler = handler;
        this.regex = Pattern.compile("^" + hostname.replaceAll("\\.", "\\\\.").replaceAll("[*]", "(.*?)") + "$", Pattern.CASE_INSENSITIVE);
    }
    @Override
    public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
        Map<String, String> headers = request.headers();
        String host = headers.get("host");
        if (host == null) {
            next.handle(null);
        } else {
            boolean match = false;
            for (String h : host.split(":")) {
                if (regex.matcher(h).find()) {
                    match = true;
                    break;
                }
            }

            if (match) {
                handler.handle(request);
            } else {
                next.handle(null);
            }
        }
    }
}
