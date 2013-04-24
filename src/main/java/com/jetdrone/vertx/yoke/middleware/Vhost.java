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
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.Map;
import java.util.regex.Pattern;

public class Vhost extends Middleware {

    private final Handler<HttpServerRequest> handler;
    private final Pattern regex;

    public Vhost(String hostname, Handler<HttpServerRequest> handler) {
        this.handler = handler;
        this.regex = Pattern.compile("^" + hostname.replaceAll("\\.", "\\\\.").replaceAll("[*]", "(.*?)") + "$", Pattern.CASE_INSENSITIVE);
    }
    @Override
    public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
        Map<String, String> headers = request.headers();
        String host = headers.get("host");
        if (host == null) {
            next.handle(null);
        } else {
            boolean match = false;
            for (String h : host.split(":")) {
                if (regex.matcher(h).find()) {
                    match = true;
                    break;
                }
            }

            if (match) {
                handler.handle(request);
            } else {
                next.handle(null);
            }
        }
    }
}
