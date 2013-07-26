/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

public class Favicon extends Middleware {

    private class Icon {
        private final Map<String, String> headers;
        private final Buffer body;

        Icon(Buffer buffer) {
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
    public Middleware init(Vertx vertx, Logger logger) {
        try {
            super.init(vertx, logger);
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
