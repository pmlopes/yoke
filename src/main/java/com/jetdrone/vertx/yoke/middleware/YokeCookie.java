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

import com.jetdrone.vertx.yoke.util.Utils;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;

import javax.crypto.Mac;
import java.util.Set;

public class YokeCookie implements Cookie {

    private final Cookie nettyCookie;
    private final Mac hmacSHA256;
    private String value;
    private boolean signed;

    public YokeCookie(Cookie nettyCookie, Mac hmacSHA256) {
        this.nettyCookie = nettyCookie;
        this.hmacSHA256 = hmacSHA256;

        // get the original value
        value = nettyCookie.getValue();
        // if the prefix is there then it is signed
        if (value.startsWith("s:")) {
            signed = true;
            // if it is signed get the unsigned value
            if (hmacSHA256 == null) {
                // this is an error
                value = null;
            } else {
                value = Utils.unsign(value.substring(2), hmacSHA256);
            }
        }
    }

    public YokeCookie(String name, Mac hmacSHA256) {
        this(new DefaultCookie(name, ""), hmacSHA256);
    }

    // extensions
    public boolean isSigned() {
        return signed;
    }

    public void sign() {
        nettyCookie.setValue("s:" + Utils.sign(value, hmacSHA256));
        signed = true;
    }

    public String getUnsignedValue() {
        return value;
    }

    @Override
    public String getValue() {
        return nettyCookie.getValue();
    }

    @Override
    public void setValue(String value) {
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
    public void setDomain(String domain) {
        nettyCookie.setDomain(domain);
    }

    @Override
    public String getPath() {
        return nettyCookie.getPath();
    }

    @Override
    public void setPath(String path) {
        nettyCookie.setPath(path);
    }

    @Override
    public String getComment() {
        return nettyCookie.getComment();
    }

    @Override
    public void setComment(String comment) {
        nettyCookie.setComment(comment);
    }

    @Override
    public long getMaxAge() {
        return nettyCookie.getMaxAge();
    }

    @Override
    public void setMaxAge(long maxAge) {
        nettyCookie.setMaxAge(maxAge);
    }

    @Override
    public int getVersion() {
        return nettyCookie.getVersion();
    }

    @Override
    public void setVersion(int version) {
        nettyCookie.setVersion(version);
    }

    @Override
    public boolean isSecure() {
        return nettyCookie.isSecure();
    }

    @Override
    public void setSecure(boolean secure) {
        nettyCookie.setSecure(secure);
    }

    @Override
    public boolean isHttpOnly() {
        return nettyCookie.isHttpOnly();
    }

    @Override
    public void setHttpOnly(boolean httpOnly) {
        nettyCookie.setHttpOnly(httpOnly);
    }

    @Override
    public String getCommentUrl() {
        return nettyCookie.getCommentUrl();
    }

    @Override
    public void setCommentUrl(String commentUrl) {
        nettyCookie.setCommentUrl(commentUrl);
    }

    @Override
    public boolean isDiscard() {
        return nettyCookie.isDiscard();
    }

    @Override
    public void setDiscard(boolean discard) {
        nettyCookie.setDiscard(discard);
    }

    @Override
    public Set<Integer> getPorts() {
        return nettyCookie.getPorts();
    }

    @Override
    public void setPorts(int... ports) {
        nettyCookie.setPorts(ports);
    }

    @Override
    public void setPorts(Iterable<Integer> ports) {
        nettyCookie.setPorts(ports);
    }

    @Override
    public int compareTo(Cookie o) {
        return nettyCookie.compareTo(o);
    }
}
