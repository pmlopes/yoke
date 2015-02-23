/**
 * Copyright 2011-2014 the original author or authors.
 */

function JSYoke() {
    this.vertx = __jvertx;
    this.container = __jcontainer;
    this.jYoke = new com.jetdrone.vertx.yoke.Yoke(this.vertx, this.container, new com.jetdrone.vertx.yoke.core.impl.JSRequestWrapper());
    // Resolve execution scope for JSYokeRequest and JSYokeResponse
    this.use(function(request, next) {
        request.resolveScope();
        next(null);
    });
}

JSYoke.prototype.store = function (sessionStore) {
    if (sessionStore) {
        // TODO: handle non Java stores
        this.jYoke.store(sessionStore);
    }
};

JSYoke.prototype.use = function (route, callback) {
    if (callback === undefined) {
        callback = route;
        route = '/';
    }

    var cbArray = java.lang.reflect.Array.newInstance(com.jetdrone.vertx.yoke.IMiddleware, 1);

    // verify if the callback is already a middleware instance
    if (callback instanceof com.jetdrone.vertx.yoke.IMiddleware) {
        // in this case pass it directly to the jYoke
        cbArray[0] = callback;
        this.jYoke.use(route, cbArray);
    } else if (typeof callback === 'object' && callback.jMiddleware !== undefined) {
        // this is a special case when base middleware (Java) is extended to be more fluent with JavaScript
        cbArray[0] = callback.jMiddleware;
        this.jYoke.use(route, cbArray);
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
        cbArray[0] = new com.jetdrone.vertx.yoke.IMiddleware(middleware);
        this.jYoke.use(route, cbArray);
    } else {
        throw new Error('callback should be a javascript function or Middleware java class');
    }

    return this;
};

JSYoke.prototype.engine = function (engine) {
    // verify if the engine is already a Engine instance
    if (engine.jEngine !== undefined) {
        // in this case pass it directly to the jYoke
        this.jYoke.engine(engine.jEngine);
    } else {
        this.jYoke.engine(new com.jetdrone.vertx.yoke.Engine(engine));
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

JSYoke.prototype.deploy = function (config, callback) {
    throw new Error('Not Implemented');
};

module.exports = JSYoke;
