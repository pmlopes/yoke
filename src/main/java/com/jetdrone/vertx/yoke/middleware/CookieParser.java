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
    public void handle(YokeHttpServerRequest request, Handler<Object> next) {
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
