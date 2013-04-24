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
package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.middleware.YokeHttpServerRequest;
import groovy.lang.Closure;
import org.vertx.groovy.core.Vertx;
import org.vertx.groovy.core.http.HttpServer;
import org.vertx.groovy.core.http.impl.DefaultHttpServerRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class GYoke extends Yoke {
    /**
     * Creates a Yoke instance.
     * This constructor should be called from a verticle and pass a valid Vertx
     * instance. This instance will be shared with all registered middleware.
     * The reason behind this is to allow middleware to use Vertx features such
     * as file system and timers.
     *
     * @param vertx The Vertx instance
     */
    public GYoke(Vertx vertx) {
        super(vertx.toJavaVertx());
    }

    /**
     * Adds a Handler to a route. The behaviour is similar to the middleware, however this
     * will be a terminal point in the execution chain. In this case any middleware added
     * after will not be executed. However you should care about the route which may lead
     * to skip this middleware.
     *
     * The idea to user a Handler is to keep the API familiar with the rest of the Vert.x
     * API.
     *
     * @see Yoke#use(String, Middleware)
     * @param route The route prefix for the middleware
     * @param handler The Handler to add
     */
    public GYoke use(String route, final Closure handler) {
        // 1 parameter is converted to Handler<HttpServerRequest>
        if (handler.getMaximumNumberOfParameters() == 1) {
            use(route, new Handler<HttpServerRequest>() {
                @Override
                public void handle(HttpServerRequest request) {
                    handler.call(new DefaultHttpServerRequest(request));
                }
            });

            return this;
        }
        // 2 parameter is converted to Middleware
        if (handler.getMaximumNumberOfParameters() == 2) {
            use(route, new Middleware() {
                @Override
                public void handle(YokeHttpServerRequest request, Handler<Object> next) {
                    handler.call(new DefaultHttpServerRequest(request), next);
                }
            });

            return this;
        }
        // otherwise error
        throw new RuntimeException("Cannot determine if closure is Handler or Middleware");
    }

    /**
     * Adds a Handler to a route.
     *
     * @see Yoke#use(String, Handler)
     * @param handler The Handler to add
     */
    public GYoke use(Closure handler) {
        return this.use("/", handler);
    }

    /**
     * Listens on an existing Groovy Server
     * @param server Groovy Server
     */
    public GYoke listen(HttpServer server) {
        super.listen(server.toJavaServer());
        return this;
    }
}
