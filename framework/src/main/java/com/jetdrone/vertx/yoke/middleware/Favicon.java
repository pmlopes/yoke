// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.middleware
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.util.Utils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.platform.Container;
import org.vertx.java.core.logging.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

// # Favicon
//
// By default serves the Yoke favicon, or the favicon located by the given ```path```.
public class Favicon extends Middleware {

    // ## Icon
    //
    // Represents a favicon.ico file and related headers
    private class Icon {
        // Headers for the icon resource
        //
        // @property headers
        // @private
        private final Map<String, String> headers;

        // Binary content of the icon file
        //
        // @property body
        // @private
        private final Buffer body;

        // Instantiate a new Icon
        //
        // @constructor
        // @param {Buffer} buffer
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

    // favicon cache
    //
    // @property icon
    // @private
    private Icon icon;

    // Location of the icon in the file system
    //
    // @property path
    // @private
    private final String path;

    // Cache control for the resource
    //
    // @property maxAge
    // @private
    private final long maxAge;

    // Create a new Favicon instance using a file in the file system and customizable cache period
    //
    // @constructor
    // @param {String} path
    // @param {long} maxAge
    //
    // @example
    //      Yoke yoke = new Yoke(...);
    //      yoke.use(new Favicon("/icons/icon.ico", 1000));
    public Favicon(String path, long maxAge) {
        this.path = path;
        this.maxAge = maxAge;
    }

    // Create a new Favicon instance using a file in the file system and cache for 1 day.
    //
    // @constructor
    // @param {String} path
    //
    // @example
    //      Yoke yoke = new Yoke(...);
    //      yoke.use(new Favicon("/icons/icon.ico"));
    public Favicon(String path) {
        this(path, 86400000);
    }

    // Create a new Favicon instance using a the default icon and cache for 1 day.
    //
    // @constructor
    //
    // @example
    //      Yoke yoke = new Yoke(...);
    //      yoke.use(new Favicon());
    public Favicon() {
        this(null);
    }

    // Loads the icon from the file system once we get a reference to Vert.x
    //
    // @internal
    // @method init
    // @param {Vertx} vertx
    // @param {Logger} logger
    // @param {String} mount
    @Override
    public Middleware init(Vertx vertx, Logger logger, String mount) {
        try {
            super.init(vertx, logger, mount);
            if (path == null) {
                icon = new Icon(Utils.readResourceToBuffer(getClass(), "favicon.ico"));
            } else {
                icon = new Icon(vertx.fileSystem().readFileSync(path));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this;
    }


    @Override
    public void handle(final YokeRequest request, final Handler<Object> next) {
        if ("/favicon.ico".equals(request.path())) {
            request.response().headers().set(icon.headers);
            request.response().end(icon.body);
        } else {
            next.handle(null);
        }
    }
}
