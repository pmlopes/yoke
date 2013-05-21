# Yoke

Yoke is a middleware framework for [Vert.x](http://www.vertx.io), shipping with over 14 bundled middleware. As with
Vert.x, Yoke tries to be a polyglot middleware framework, currently *Java*, *Groovy* and *JavaScript* are supported
languages and can be used interchangeably like with other Vert.x components.

Yoke also does templating with its abstract template engine that can be extended to support any format needed. Out
of the box, *Java* bundles a simple *StringPlaceholderEngine*, *Groovy* bundles the *GroovyTemplate* and *JavaScript*
bundles the *MicroTemplateEngine*.


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

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines}
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

### Vert.x module

Vert.x 2 module: ```com.jetdrone~yoke~1.0.0-beta2```

### Maven artifact

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.xml}
<dependency>
  <groupId>com.jetdrone</groupId>
  <artifactId>yoke</artifactId>
  <version>1.0.0-beta2</version>
  <scope>provided</scope>
</dependency>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


## Extensions to Vert.x

* [YokeRequest](YokeRequest.html) - The internal augmented request object.
* [YokeResponse](YokeResponse.html) - The internal augmented response object.


## Tutorials

* [Java](Java-Tutorial.html) Java Tutorials
* [Groovy](Groovy-Tutorial.html) Groovy Tutorials
* [JavaScript](JavaScript-Tutorial.html) JavaScript Tutorials


## Example Apps

* [KitCMS](https://github.com/pmlopes/yoke/tree/master/example/kitcms) - Remake of KitCMS from NodeJS with Yoke
* [Persona](https://github.com/pmlopes/yoke/tree/master/example/persona) - Implementation of Mozilla Persona Authentication with Yoke


## Middleware

* [BasicAuth](BasicAuth.html) basic http authentication
* [BodyParser](BodyParser.html) extensible request body parser
* [CookieParser](CookieParser.html) cookie parser
* [ErrorHandler](ErrorHandler.html) flexible error handler
* [Favicon](Favicon.html) efficient favicon server (with default icon)
* [Limit](Limit.html) limit the bytesize of request bodies
* [Logger](Logger.html) Access request logger
* [MethodOverride](MethodOverride.html) faux HTTP method support
* [ResponseTime](ResponseTime.html) calculates response-time and exposes via x-response-time
* [Router](Router.html) flexible routing based on RouteMatcher
* [Session](Session.html) Cookie session
* [Static](Static.html) streaming static file server supporting directory listings
* [Timeout](Timeout.html) request timeouts
* [Vhost](Vhost.html) virtual host sub-domain mapping middleware


## Engines

* [StringPlaceholderEngine](StringPlaceholderEngine.html) StringPlaceholderEngine
* [GroovyTemplate](GroovyTemplate.html) GroovyTemplate
* [MicroTemplateEngine](MicroTemplateEngine.html) MicroTemplateEngine

## Benchmarks

* [Benchmark](Benchmark.html) Simple Benchmarks Yoke vs [ExpressJS](http://expressjs.com)


## 3rd-party

* [Google Closure templates](https://github.com/core9/vertx-yoke-engine-closure) Yoke templating engine for Google Closure templates


## Links

* GitHub [repository](https://github.com/pmlopes/yoke)
* [Vert.x](http://vertx.io)