// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.middleware
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import org.vertx.java.core.Handler;

import javax.crypto.Mac;
import java.util.Set;
import java.util.TreeSet;

public class CookieParser extends Middleware {

    private final Mac hmacSHA256;

    public CookieParser(Mac hmacSHA256) {
        this.hmacSHA256 = hmacSHA256;
    }

    public CookieParser() {
        this(null);
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        String cookieHeader = request.getHeader("cookie");

        if (cookieHeader != null) {
            Set<Cookie> nettyCookies = CookieDecoder.decode(cookieHeader);
            Set<YokeCookie> cookies = new TreeSet<>();

            for (Cookie cookie : nettyCookies) {
                YokeCookie yokeCookie = new YokeCookie(cookie, hmacSHA256);
                String value = yokeCookie.getUnsignedValue();
                // value cannot be null in a cookie if the signature is mismatch then this value will be null
                // in that case the cookie has been tampered
                if (value == null) {
                    next.handle(400);
                    return;
                }
                cookies.add(yokeCookie);
            }

            request.setCookies(cookies);
        }

        next.handle(null);
    }
}
