/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.AbstractMiddleware;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * # Favicon
 *
 * By default serves the Yoke favicon, or the favicon located by the given ```path```.
 */
public class Favicon extends AbstractMiddleware {

    /**
     * ## Icon
     *
     * Represents a favicon.ico file and related headers
     */
    private class Icon {
        /**
         * Headers for the icon resource
         */
        private final Map<String, String> headers;

        /**
         * Binary content of the icon file
         */
        private final Buffer body;

        /**
         * Instantiate a new Icon
         *
         * @param buffer buffer containing the image data for this icon.
         */
        private Icon(Buffer buffer) {
            headers = new HashMap<>();
            body = buffer;

            headers.put("content-type", "image/x-icon");
            headers.put("content-length", Integer.toString(buffer.length()));

            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                headers.put("etag", "\"" + Utils.base64(md.digest(buffer.getBytes())) + "\"");
            } catch (NoSuchAlgorithmException e) {
                // ignore
            }
            headers.put("cache-control", "public, max-age=" + (maxAge / 1000));
        }
    }

    /**
     * favicon cache
     */
    private Icon icon;

    /**
     * Location of the icon in the file system
     */
    private final String path;

    /**
     * Cache control for the resource
     */
    private final long maxAge;

    /**
     * Create a new Favicon instance using a file in the file system and customizable cache period
     *
     * <pre>
     * Yoke yoke = new Yoke(...);
     * yoke.use(new Favicon("/icons/icon.ico", 1000));
     * </pre>
     *
     * @param path
     * @param maxAge
     */
    public Favicon(final String path, final long maxAge) {
        this.path = path;
        this.maxAge = maxAge;
    }

    /**
     * Create a new Favicon instance using a file in the file system and cache for 1 day.
     *
     * <pre>
     * Yoke yoke = new Yoke(...);
     * yoke.use(new Favicon("/icons/icon.ico"));
     * </pre>
     *
     * @param path
     */
    public Favicon(String path) {
        this(path, 86400000);
    }

    /**
     * Create a new Favicon instance using a the default icon and cache for 1 day.
     *
     * <pre>
     * Yoke yoke = new Yoke(...);
     * yoke.use(new Favicon());
     * </pre>
     */
    public Favicon() {
        this(null);
    }

    /**
     * Loads the icon from the file system once we get a reference to Vert.x
     *
     * @param yoke
     * @param mount
     */
    @Override
    public Middleware init(@NotNull final Yoke yoke, @NotNull final String mount) {
        try {
            super.init(yoke, mount);
            if (path == null) {
                icon = new Icon(Utils.readResourceToBuffer(getClass(), "favicon.ico"));
            } else {
                icon = new Icon(fileSystem().readFileSync(path));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this;
    }


    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
        if ("/favicon.ico".equals(request.normalizedPath())) {
            request.response().headers().set(icon.headers);
            request.response().end(icon.body);
        } else {
            next.handle(null);
        }
    }
}
