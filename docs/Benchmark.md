# [Yoke](/)

## Benchmarks

Yoke has a simple benchmark to compare to ExpressJS, these benchmarks are too simple and do not represent real world
scenarios. Proper benchmarking should be done comparing what you need and not just serving static text or json
resources.

## ExpressJS code

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.javascript}
var express = require('express');
var app = express();

app.configure(function(){
    app.use(express.bodyParser());
});

app.get('/', function(req, res){
    res.send('Hello World\n');
});

app.get('/json', function(req, res){
    res.send({ name: 'Tobi', role: 'admin' });
});

function foo(req, res, next) {
    next();
}

app.get('/middleware', foo, foo, foo, foo, function(req, res){
    res.send('1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890');
});

app.listen(8000);
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This application was running node *v0.10.3* and express *3.2.4* and in a single thread.


## Yoke code

Due to differences between *express* and *yoke* APIs yoke code had 2 versions. The first version was used to test *text*
and *json* resources and the second, to test the *middleware* resource to have the same 4 hops as on *express* before
reaching the resource.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
public class YokeBench extends Verticle {

    @Override
    public void start() {
        new Yoke(vertx)
                .use(new BodyParser())
                .use(new Router()
                        .get("/", new Handler<YokeRequest>() {
                            @Override
                            public void handle(YokeRequest request) {
                                request.response().end("Hello World\n");
                            }
                        })
                        .get("/json", new Handler<YokeRequest>() {
                            @Override
                            public void handle(YokeRequest request) {
                                request.response().end(new JsonObject().putString("name", "Tobi").putString("role", "admin"));
                            }
                        })
                ).listen(8080);
    }
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The seconds code to test only the *middleware* resource.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
public class YokeBench extends Verticle {

    @Override
    public void start() {

        final Middleware foo = new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                next.handle(null);
            }
        };

        new Yoke(vertx)
                .use(new BodyParser())
                // this should be only here to test the middleware resource
                .use(foo)
                .use(foo)
                .use(foo)
                .use(foo)
                .use(new Router()
                        .get("/", new Handler<YokeRequest>() {
                            @Override
                            public void handle(YokeRequest request) {
                                request.response().end("Hello World\n");
                            }
                        })
                        .get("/json", new Handler<YokeRequest>() {
                            @Override
                            public void handle(YokeRequest request) {
                                request.response().end(new JsonObject().putString("name", "Tobi").putString("role", "admin"));
                            }
                        })
                        .get("/middleware", new Handler<YokeRequest>() {
                            @Override
                            public void handle(YokeRequest request) {
                                request.response().end("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
                            }
                        })
                ).listen(8080);
    }
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This application was running java *1.7.0_21 (Oracle)*, yoke *1.0.0-SNAPSHOT* and vert.x *2.0.0-SNAPSHOT* and in a single
thread.


## Results

### Serving text

![Hello World expressJS vs Yoke](text.png)

As it can be seem Yoke is faster and scales the same way ExpressJS does.

### Serving json

![JSON expressJS vs Yoke](json.png)

As it can be seem Yoke is faster and scales the same way ExpressJS does.

### Middleware

![Middleware expressJS vs Yoke](middleware.png)

As it can be seem Yoke is faster and scales the same way ExpressJS does.


## Conclusions

These benchmarks are quite naive but give you a good impression that if you have a fast expressJS application and you
port it to Yoke you will get some more performance and the expected scalability.