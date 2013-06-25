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

public class ResponseTime extends Middleware {
    @Override
    public void handle(YokeRequest request, Handler<Object> next) {

        final long start = System.currentTimeMillis();
        final YokeResponse response = request.response();

        response.headersHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                long duration = System.currentTimeMillis() - start;
                response.putHeader("x-response-time", duration + "ms");
            }
        });

        next.handle(null);
    }
}
