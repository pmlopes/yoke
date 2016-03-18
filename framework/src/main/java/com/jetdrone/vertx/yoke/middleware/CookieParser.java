/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.core.YokeCookie;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;

import javax.crypto.Mac;
import java.util.Set;
import java.util.TreeSet;

/**
 * # CookieParser
 *
 * Parse request cookies both signed or plain.
 *
 * If a cooke value starts with *s:* it means that it is a signed cookie. In this case the value is expected to be
 * *s:&lt;cookie&gt;.&lt;signature&gt;*. The signature is *HMAC + SHA256*.
 *
 * When the Cookie parser is initialized with a secret then that value is used to verify if a cookie is valid.
 */
public class CookieParser extends Middleware {

    /**
     * Message Signer
     */
    private final Mac mac;

    /**
     * Instantiates a CookieParser with a given Mac.
     *
     * <pre>
     * Yoke yoke = new Yoke(...);
     * yoke.use(new CookieParser(YokeSecurity.newHmacSHA256("s3cr3t")));
     * </pre>
     *
     * @param mac Mac
     */
    public CookieParser(final Mac mac) {
        this.mac = mac;
    }

    /**
     * Instantiates a CookieParser without a Mac. In this case no cookies will be signed.
     *
     * <pre>
     * Yoke yoke = new Yoke(...);
     * yoke.use(new CookieParser());
     * </pre>
     */
    public CookieParser() {
        this(null);
    }

    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
        String cookieHeader = request.getHeader("cookie");

        if (cookieHeader != null) {
            Set<Cookie> nettyCookies = CookieDecoder.decode(cookieHeader);
            Set<YokeCookie> cookies = new TreeSet<>();

            for (Cookie cookie : nettyCookies) {
                YokeCookie yokeCookie = new YokeCookie(cookie, mac);
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
