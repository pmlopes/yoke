# Yoke

Yoke is a middleware framework for [Vert.x](http://www.vertx.io), shipping with over 12 bundled middleware. As with
Vert.x, Yoke tries to be a polyglot middleware framework, currently *Java*, *Groovy* and *JavaScript* are supported
languages and can be used interchangeably like with other Vert.x components.

### Java

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines}
Yoke yoke = new Yoke(vertx)
  .use(new Favicon())
  .use(new Static("webroot"))
  .use(new Router()
    .all("/hello", new Handler<HttpServerRequest>() {
      @Override
      public void handle(HttpServerRequest request) {
        request.response().end("Hello World!");
      }
    })).listen(3000);
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

### Groovy

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.boo .numberLines}
def yoke = new GYoke(vertx)
  .use(new Favicon())
  .use(new Static("webroot"))
  .use(new GRouter()
    .all("/hello") { request ->
      request.response().end("Hello World!");
    }).listen(3000)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

### JavaScript

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.javascript .numberLines}
var yoke = new Yoke()
  .use(new Favicon())
  .use(new Static('webroot'))
  .use(new Router()
    .all("/hello", function (request) {
      request.response().end('Hello World!');
    })).listen(3000);
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


## Installation

Vert.x 2 module: ```com.jetdrone~yoke~1.0.0-SNAPSHOT```

Maven artifact:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.xml}
<dependency>
  <groupId>com.jetdrone</groupId>
  <artifactId>yoke</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <scope>provided</scope>
</dependency>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


## API

* [com.jetdrone.vertx.yoke.Yoke](com.jetdrone.vertx.yoke.Yoke.html) The Framework main class
* [com.jetdrone.vertx.yoke.MimeType](com.jetdrone.vertx.yoke.MimeType.html) Mime-Types utils
* [com.jetdrone.vertx.yoke.Middleware](com.jetdrone.vertx.yoke.Middleware.html) Abstract class that all midleware extends
* [com.jetdrone.vertx.yoke.Engine](com.jetdrone.vertx.yoke.Engine.html) Abstract class that future render engines extend
* [com.jetdrone.vertx.yoke.middleware.YokeRequest](com.jetdrone.vertx.yoke.middleware.YokeRequest.html) Implementation of HttpServerRequest with some extra helper fields
* [com.jetdrone.vertx.yoke.middleware.YokeResponse](com.jetdrone.vertx.yoke.middleware.YokeResponse.html) Implementation of HttpServerResponse with suport for render engines


## Middleware

* [BasicAuth](BasicAuth.html) basic http authentication
* [BodyParser](BodyParser.html) extensible request body parser
* [CookieParser](CookieParser.html) cookie parser
* [ErrorHandler](ErrorHandler.html) flexible error handler
* [Favicon](Favicon.html) efficient favicon server (with default icon)
* [Limit](Limit.html) limit the bytesize of request bodies
* [MethodOverride](MethodOverride.html) faux HTTP method support
* [ResponseTime](ResponseTime.html) calculates response-time and exposes via x-response-time
* [Router](Router.html) flexible routing based on RouteMatcher
* [Static](Static.html) streaming static file server supporting directory listings
* [Timeout](Timeout.html) request timeouts
* [Vhost](Vhost.html) virtual host sub-domain mapping middleware


## Links

* GitHub [repository](https://github.com/pmlopes/yoke)