# Yoke

Yoke is a middleware framework for [Vert.x](http://www.vertx.io), shipping with over 12 bundled middleware.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines}
Yoke yoke = new Yoke(vertx);

yoke.use(new Favicon());
yoke.use(new Static("webroot"));
yoke.use(new Router() {{
  all("/hello", new Handler<HttpServerRequest>() {
    @Override
    public void handle(HttpServerRequest request) {
      request.response().end("Hello World!");
    }
  });
}});

yoke.listen(3000);
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
* [com.jetdrone.vertx.yoke.middleware.YokeHttpServerRequest](com.jetdrone.vertx.yoke.middleware.YokeHttpServerRequest.html) Implementation of HttpServerRequest with some extra helper fields
* [com.jetdrone.vertx.yoke.middleware.YokeHttpServerResponse](com.jetdrone.vertx.yoke.middleware.YokeHttpServerResponse.html) Implementation of HttpServerResponse with suport for render engines

## Middleware

* <a href="BasicAuth.html">BasicAuth</a> basic http authentication
* <a href="BodyParser.html">BodyParser</a> extensible request body parser
* <a href="CookieParser.html">CookieParser</a> cookie parser
* <a href="ErrorHandler.html">ErrorHandler</a> flexible error handler
* <a href="Favicon.html">Favicon</a> efficient favicon server (with default icon)
* <a href="Limit.html">Limit</a> limit the bytesize of request bodies
* <a href="MethodOverride.html">MethodOverride</a> faux HTTP method support
* <a href="ResponseTime.html">ResponseTime</a> calculates response-time and exposes via x-response-time
* <a href="Router.html">Router</a> flexible routing based on RouteMatcher
* <a href="Static.html">Static</a> streaming static file server supporting directory listings
* <a href="Timeout.html">Timeout</a> request timeouts
* <a href="Vhost.html">Vhost</a> virtual host sub-domain mapping middleware

## Links

* GitHub <a href="https://github.com/pmlopes/yoke">repository</a>