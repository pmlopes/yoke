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
import org.vertx.java.core.json.JsonObject;

import java.util.Map;

public class MethodOverride extends Middleware {

    private final String key;

    public MethodOverride(String key) {
        this.key = key;
    }

    public MethodOverride() {
        this("_method");
    }

    @Override
    public void handle(final YokeHttpServerRequest request, final Handler<Object> next) {
        final Object body = request.body();

        if (request.hasBody() && body != null) {
            if (body instanceof Map) {
                Object method = ((Map) body).get(key);
                if (method != null) {
                    if (method instanceof String) {
                        ((Map) body).remove(key);
                        request.setMethod((String) method);
                    }
                }
            } else if (body instanceof JsonObject) {
                String method = ((JsonObject) body).getString(key);
                if (method != null) {
                    ((JsonObject) body).removeField(key);
                    request.setMethod(method);
                }
            }
        } else {
            String xHttpMethodOverride = request.headers().get("x-http-setMethod-override");

            if (xHttpMethodOverride != null) {
                request.setMethod(xHttpMethodOverride);
            }
        }

        next.handle(null);
    }
}
