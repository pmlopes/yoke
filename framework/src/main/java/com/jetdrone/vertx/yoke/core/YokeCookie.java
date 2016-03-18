/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.core;

import com.jetdrone.vertx.yoke.YokeSecurity;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;

/**
 * # YokeCookie
 */
public class YokeCookie implements Cookie {

    private final Cookie nettyCookie;
    private final Mac mac;
    private String value;
    private boolean signed;

    public YokeCookie(@NotNull final Cookie nettyCookie, final Mac mac) {
        this.nettyCookie = nettyCookie;
        this.mac = mac;

        // get the original value
        value = nettyCookie.value();
        // if the prefix is there then it is signed
        if (value.startsWith("s:")) {
            signed = true;
            // if it is signed get the unsigned value
            if (mac == null) {
                // this is an error
                value = null;
            } else {
                value = YokeSecurity.unsign(value.substring(2), mac);
            }
        }
    }

    public YokeCookie(@NotNull final String name, final Mac mac) {
        this(new DefaultCookie(name, ""), mac);
    }

    public YokeCookie(@NotNull final String name, @NotNull final String value) {
        this(new DefaultCookie(name, value), null);
    }

    // extensions
    public boolean isSigned() {
        return signed;
    }

    public void sign() {
        if (mac != null) {
            nettyCookie.setValue("s:" + YokeSecurity.sign(value, mac));
            signed = true;
        } else {
            signed = false;
        }
    }

    public String getUnsignedValue() {
        return value;
    }

    @Override
    public String value() {
        return nettyCookie.value();
    }

    @Override
    public void setValue(final String value) {
        this.value = value;
        this.signed = false;
        nettyCookie.setValue(value);
    }

    @Override
    public boolean wrap() {
        return nettyCookie.wrap();
    }

    @Override
    public void setWrap(boolean wrap) {
        nettyCookie.setWrap(wrap);
    }

    @Override
    public String name() {
        return nettyCookie.name();
    }

    @Override
    public String domain() {
        return nettyCookie.domain();
    }

    @Override
    public void setDomain(final String domain) {
        nettyCookie.setDomain(domain);
    }

    @Override
    public String path() {
        return nettyCookie.path();
    }

    @Override
    public void setPath(final String path) {
        nettyCookie.setPath(path);
    }

    @Override
    public long maxAge() {
        return nettyCookie.maxAge();
    }

    @Override
    public void setMaxAge(final long maxAge) {
        nettyCookie.setMaxAge(maxAge);
    }

    @Override
    public boolean isSecure() {
        return nettyCookie.isSecure();
    }

    @Override
    public void setSecure(final boolean secure) {
        nettyCookie.setSecure(secure);
    }

    @Override
    public boolean isHttpOnly() {
        return nettyCookie.isHttpOnly();
    }

    @Override
    public void setHttpOnly(final boolean httpOnly) {
        nettyCookie.setHttpOnly(httpOnly);
    }

    @Override
    public int compareTo(@NotNull final Cookie o) {
        return nettyCookie.compareTo(o);
    }
}
