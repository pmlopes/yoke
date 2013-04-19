package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public class CookieParser extends Middleware {

    private final String secret;
    private final Mac hmacSHA256;

    public CookieParser(String secret) {
        try {
            this.secret = secret;
            hmacSHA256 = Mac.getInstance("HmacSHA256");
            hmacSHA256.init(new SecretKeySpec(secret.getBytes(), hmacSHA256.getAlgorithm()));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public CookieParser() {
        secret = null;
        hmacSHA256 = null;
    }

    public String sign(String val) {
        hmacSHA256.reset();
        return val + "." + Utils.base64(hmacSHA256.doFinal(val.getBytes()));
    }

    public String unsign(String val) {
        String str = val.substring(0, val.lastIndexOf('.'));
        if (val.equals(sign(str))) {
            return str;
        }
        return null;
    }

    @Override
    public void handle(YokeHttpServerRequest request, Handler<Object> next) {
        String cookieHeader = request.headers().get("cookie");

        if (cookieHeader != null) {
            Set<Cookie> cookies = CookieDecoder.decode(cookieHeader);

            if (secret != null) {
                for (Cookie cookie : cookies) {
                    String value = cookie.getValue();
                    if (value != null) {
                        if (value.startsWith("s:")) {
                            String unsignedValue = unsign(value.substring(2));
                            if (unsignedValue == null) {
                                next.handle(400);
                                return;
                            }
                            cookie.setValue(unsignedValue);
                        }
                    }
                }
            }

            request.cookies(cookies);
        }

        next.handle(null);
    }
}
