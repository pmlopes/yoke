package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class Favicon extends Middleware {

    private class Icon {
        private final Map<String, Object> headers;
        private final Buffer body;

        Icon(Buffer buffer) {
            headers = new HashMap<>();
            body = buffer;

            headers.put("content-type", "image/x-icon");
            headers.put("content-length", buffer.length());

            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                headers.put("etag", "\"" + Utils.base64(md.digest(buffer.getBytes())) + "\"");
            } catch (NoSuchAlgorithmException e) {
                // ignore
            }
            headers.put("cache-control", "public, max-age=" + (maxAge / 1000));
        }
    }

    // favicon cache
    private Icon icon;
    private final String path;
    private final long maxAge;

    public Favicon(String path, long maxAge) {
        this.path = path;
        this.maxAge = maxAge;
    }

    public Favicon(String path) {
        this(path, 86400000);
    }

    public Favicon() {
        this(null);
    }

    @Override
    public void setVertx(Vertx vertx) {
        try {
            super.setVertx(vertx);
            String iconPath;

            if (path == null) {
                iconPath = Favicon.class.getResource("favicon.ico").getPath();
            } else {
                iconPath = path;
            }

            icon = new Icon(vertx.fileSystem().readFileSync(iconPath));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
        if ("/favicon.ico".equals(request.path())) {
            for (Map.Entry<String, Object> header : icon.headers.entrySet()) {
                request.response().putHeader(header.getKey(), header.getValue());
            }
            request.response().end(icon.body);
        } else {
            next.handle(null);
        }
    }
}
