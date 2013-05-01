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
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public class CookieParser extends Middleware {

    private final Mac hmacSHA256;

    public CookieParser(String secret) {
        try {
            hmacSHA256 = Mac.getInstance("HmacSHA256");
            hmacSHA256.init(new SecretKeySpec(secret.getBytes(), hmacSHA256.getAlgorithm()));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public CookieParser() {
        hmacSHA256 = null;
    }

    @Override
    public void handle(YokeHttpServerRequest request, Handler<Object> next) {
        String cookieHeader = request.getHeader("cookie");

        if (cookieHeader != null) {
            Set<Cookie> cookies = CookieDecoder.decode(cookieHeader);

            if (hmacSHA256 != null) {
                for (Cookie cookie : cookies) {
                    String value = cookie.getValue();
                    if (value != null) {
                        if (value.startsWith("s:")) {
                            String unsignedValue = Utils.unsign(value.substring(2), hmacSHA256);
                            if (unsignedValue == null) {
                                next.handle(400);
                                return;
                            }
                            cookie.setValue(unsignedValue);
                        }
                    }
                }
            }

            request.setCookies(cookies);
        }

        next.handle(null);
    }
}
