/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.core;

import com.jetdrone.vertx.yoke.security.YokeSecurity;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import java.util.Set;

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
        value = nettyCookie.getValue();
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
    public String getValue() {
        return nettyCookie.getValue();
    }

    @Override
    public void setValue(final String value) {
        this.value = value;
        this.signed = false;
        nettyCookie.setValue(value);
    }

    @Override
    public String getName() {
        return nettyCookie.getName();
    }

    @Override
    public String getDomain() {
        return nettyCookie.getDomain();
    }

    @Override
    public void setDomain(final String domain) {
        nettyCookie.setDomain(domain);
    }

    @Override
    public String getPath() {
        return nettyCookie.getPath();
    }

    @Override
    public void setPath(final String path) {
        nettyCookie.setPath(path);
    }

    @Override
    public String getComment() {
        return nettyCookie.getComment();
    }

    @Override
    public void setComment(final String comment) {
        nettyCookie.setComment(comment);
    }

    @Override
    public long getMaxAge() {
        return nettyCookie.getMaxAge();
    }

    @Override
    public void setMaxAge(final long maxAge) {
        nettyCookie.setMaxAge(maxAge);
    }

    @Override
    public int getVersion() {
        return nettyCookie.getVersion();
    }

    @Override
    public void setVersion(final int version) {
        nettyCookie.setVersion(version);
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
    public String getCommentUrl() {
        return nettyCookie.getCommentUrl();
    }

    @Override
    public void setCommentUrl(final String commentUrl) {
        nettyCookie.setCommentUrl(commentUrl);
    }

    @Override
    public boolean isDiscard() {
        return nettyCookie.isDiscard();
    }

    @Override
    public void setDiscard(final boolean discard) {
        nettyCookie.setDiscard(discard);
    }

    @Override
    public Set<Integer> getPorts() {
        return nettyCookie.getPorts();
    }

    @Override
    public void setPorts(final int... ports) {
        nettyCookie.setPorts(ports);
    }

    @Override
    public void setPorts(final Iterable<Integer> ports) {
        nettyCookie.setPorts(ports);
    }

    @Override
    public int compareTo(@NotNull final Cookie o) {
        return nettyCookie.compareTo(o);
    }
}
