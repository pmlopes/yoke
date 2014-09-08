# Java API

Yoke is a minimal and flexible Vert.x web application framework, providing a robust set of features for building single
and multi-page, and hybrid web applications. Yoke provides a thin layer of features fundamental to any web application,
without obscuring features that you know and love in Vert.x.

Before you start, you need to understand the basic functionality under Yoke. Yoke is a chain executor, modelled in a way
like the [chain-of-responsability pattern](http://en.wikipedia.org/wiki/Chain-of-responsibility_pattern). Yoke is
responsible to execute all your processing objects, which from now on we call Middleware. Middleware makes it easier for
software developers to perform specialized tasks, such as *HTTP Parsing*, *Authentication*, *Routing*, etc..., so they
can focus on the specific purpose of their application.

The API of Yoke is quite simple, in fact it consist only of 7 methods:

* ```engine``` - API to register render engines. A render engine is a simple interface to a template engine, by default
  Yoke ships with a simple placeholder replacer, however there are implementations for other popular template languages
  like:
    * ```jade```
    * ```handlebars```
    * ```mvel```
    * ```thymeleaf```
* ```store``` - API to register storage engines. A storage engine is a interface to keep session data, currently there
  are the following storage backends:
    * ```SharedDataSessionStore``` - Backend backed by Vert.x ```SharedData```. Currently this backend has a limitation
       that the data is only shared in the same JVM, not across the cluster, however this is a implementation limitation
       of the Vert.x API not of the backend.
    * ```RedisSessionStore``` - Backend backed by a [redis](http://www.redis.io) server.
    * ```MongoDbSessionStore``` - Backend backed by a [mongodb](http://www.mongodb.org) server.
* ```set``` - API to store any value in the global context. Configuration properties, etc....
* ```use``` - API to add ```Middleware``` to the chain. This is the method that you will need to use more often, the
  order that the middleware is executed is the same as the registration.
* ```listen``` - API to bind this application to a HttpServer, either ```HTTP``` or ```HTTPS```.
* ```deploy``` - Helper API to deploy other modules in order to facilitate beter development of Vert.x applications.
* ```security``` - API to handle all sort of encryption, such as provide cyphers to cookie parsers, decode web tokens,
  etc...
  
# Hello World

At this moment you should understand the basic API of Yoke, and how it works so it is time to write your first *Hello
World* example.

## Bootstrap a Java Project

## Code

Assuming you already bootstraped your project and are inside the working directory we are going to create the class:
```com.jetdrone.yoke.HelloWorldVerticle``` under ```src/main/java```. At this moment you should have the file
```src/main/java/com/jetdrone/yoke/HelloWorldVerticle.java``` in your file system.

The reason we have a *Verticle* class is because Yoke does not forces you to use any special classes, you still code
like you would on Vert.x. So we start by creating a Verticle the same way we would on any other Vert.x application:

    package com.jetdrone.vertx;

    import com.jetdrone.vertx.yoke.*;
    import com.jetdrone.vertx.yoke.middleware.*;

    import org.vertx.java.core.*;
    import org.vertx.java.platform.*;

    public class HelloWorldVerticle extends Verticle {
      public void start() {
      }
    }

This is the bare minimal Verticle code you need to write. Of course this code does not do anything fancy, in fact it
just do nothing. So lets make a *HTTP* web server that listens on port *8080*.

    ...
      public void start() {
        Yoke yoke = new Yoke(vertx);
        yoke.listen(8080);
      }
    ...

At this point you have almost everything needed to run your verticle, the only missing item is the module descriptor.
The module descriptor is a simple *JSON* file that describes the main verticle and any other metadata required for your
verticle.

For this example we only need to define 2 entries, the *main* verticle class and the dependency on *Yoke*. Create a file
under ```src/main/resources/mod.json``` with the following content:

    {
      "main": "com.jetdrone.vertx.HelloWorldVerticle",
      "includes": "com.jetdrone~yoke~2.0.4"
    }

And you can quickly test your new Verticle by running:

    ~/Projects/yoke-tutorial$ ./gradlew runMod
    ~/Projects/yoke-tutorial$

If you open a web browser and navigate to your localhost at port 8080 you will get a *404* *Not Found* error message.
This is expected since you have a server running but no middleware is handling your requests. So lets install a handler
for all request paths to respond with "Hello World".

    ...
      public void start() {
        Yoke yoke = new Yoke(vertx);
        yoke.use(new Handler<YokeRequest>() {
          @Override
          public void handle(YokeRequest request) {
            request.response().end("Hello World!");
          }
        });
        yoke.listen(8080);
      }
    ...

If you restart your verticle running the gradle command from before and reload your web browser you will see the text
"Hello World" on it.

## Testing
