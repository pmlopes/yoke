# Yoke

Yoke is a middleware framework for [Vert.x](http://www.vertx.io), shipping with over 14 bundled middleware. As with
Vert.x, Yoke tries to be a polyglot middleware framework, currently *Java*, *Groovy* and *JavaScript* are supported
languages and can be used interchangeably like with other Vert.x components.

Yoke also does templating with its abstract template engine that can be extended to support any format needed. Out
of the box, *Java* bundles a simple *StringPlaceholderEngine*, *Groovy* bundles the *GroovyTemplate* and *JavaScript*
bundles the *MicroTemplateEngine*.


### Java

``` java
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
```

### Groovy

``` groovy
def yoke = new GYoke(vertx)
  .use(new Favicon())
  .use(new Static("webroot"))
  .use(new GRouter()
    .all("/hello") { request ->
      request.response().end("Hello World!");
    }).listen(3000)
```

### JavaScript

``` js
var yoke = new Yoke()
  .use(new Favicon())
  .use(new Static('webroot'))
  .use(new Router()
    .all("/hello", function (request) {
      request.response().end('Hello World!');
    })).listen(3000);
```


## Installation

### Vert.x module

Vert.x 2 module:

```com.jetdrone~yoke~1.0.4```

### Maven artifact

``` xml
<dependency>
  <groupId>com.jetdrone</groupId>
  <artifactId>yoke</artifactId>
  <version>1.0.4</version>
  <scope>provided</scope>
</dependency>
```


## Tutorials

* [Java](Java-Tutorial.html) Java Tutorials
* [Groovy](Groovy-Tutorial.html) Groovy Tutorials
* [JavaScript](JavaScript-Tutorial.html) JavaScript Tutorials


## Example Apps

* [KitCMS](https://github.com/pmlopes/yoke/tree/master/example/kitcms) - Remake of KitCMS from NodeJS with Yoke
* [Persona](Persona.html) - Implementation of Mozilla Persona Authentication with Yoke


## Benchmarks

* [Benchmark](Benchmark.html) Simple Benchmarks Yoke vs [ExpressJS](http://expressjs.com)


## Extras

* [Rest](Rest.html) Json REST Router


## 3rd-party

* [Google Closure templates](https://github.com/core9/vertx-yoke-engine-closure) Yoke templating engine for Google Closure templates


## Links

* GitHub [repository](https://github.com/pmlopes/yoke)
* [Vert.x](http://vertx.io)
* [Google Group](https://groups.google.com/forum/?fromgroups#!forum/yoke-framework)
* [Me](http://www.jetdrone.com)