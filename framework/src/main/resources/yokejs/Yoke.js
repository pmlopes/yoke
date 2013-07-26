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

function JSYoke() {
    this.vertx = __jvertx;
    this.jYoke = new com.jetdrone.vertx.yoke.Yoke(this.vertx, __jcontainer.logger());
}

JSYoke.prototype.use = function (route, callback) {
    if (callback === undefined) {
        callback = route;
        route = '/';
    }

    // verify if the callback is already a middleware instance
    if (callback instanceof com.jetdrone.vertx.yoke.Middleware) {
        // in this case pass it directly to the jYoke
        this.jYoke.use(route, callback);
    } else if (typeof callback === 'object' && callback.jMiddleware !== undefined) {
        // this is a special case when base middleware (Java) is extended to be more fluent with JavaScript
        this.jYoke.use(route, callback.jMiddleware);
    } else if (typeof callback === 'function') {
        // wrap the function into a Middleware Java class
        var middleware = {
            handle: function (request, next) {
                callback(request, function (error) {
                    if (error === undefined) {
                        next.handle(null);
                    } else {
                        next.handle(error);
                    }
                });
            }
        };
        this.jYoke.use(route, new com.jetdrone.vertx.yoke.Middleware(middleware));
    } else {
        throw new Error('callback should be a javascript function or Middleware java class');
    }

    return this;
};

JSYoke.prototype.engine = function (extension, engine) {
    // verify if the engine is already a Engine instance
    if (engine.jEngine !== undefined) {
        // in this case pass it directly to the jYoke
        this.jYoke.engine(extension, engine.jEngine);
    } else {
        this.jYoke.engine(extension, new com.jetdrone.vertx.yoke.Engine(engine));
    }
};

JSYoke.prototype.set = function (key, value) {
    this.jYoke.set(key, value);
    return this;
};

JSYoke.prototype.listen = function (port, address) {
    // check if port looks like an HTTP server as created by vertx.createHttpServer
    if (typeof port === 'object' && port._to_java_server() instanceof org.vertx.java.core.http.HttpServer) {
        // in that case pass it directly to the jYoke
        this.jYoke.listen(port._to_java_server());
    } else {
        if (address === undefined) {
            address = '0.0.0.0';
        }

        this.jYoke.listen(port, address);
    }
};

module.exports = JSYoke;
