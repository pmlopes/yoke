# [Yoke](/)

## Java Tutorial

## Boostraping a Project

There are several options on creating projects for Java. In this tutorial the chosen method is using
[Gradle](http://www.gradle.org/). The choice was made because there is no required installation of software (as long as
you have *JDK >= 1.7* installed in your system.

It is assumed that the reader has basic knowledge of *git*, *shell* scripting and of course *Java*. You are not required
to know *gradle* since we are using this template project.

Start by downloading the [yoke-gradle-template](yoke-gradle-template.tar.gz) to your local development work directory.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
pmlopes@jetdrone:~/Projects$ curl -0 http://pmlopes.github.io/yoke/yoke-gradle-template.tar.gz | tar -zx
pmlopes@jetdrone:~/Projects$
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The extracted project lives under the directory *yoke-gradle-template* it should be expected to be renamed to a better
suitable name describing the project to be implemented.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
pmlopes@jetdrone:~/Projects$ mv yoke-gradle-template yoke-tutorial
pmlopes@jetdrone:~/Projects$ cd yoke-tutorial
pmlopes@jetdrone:~/Projects/yoke-tutorial$
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


## Introduction

Yoke is a minimal and flexible Vert.x web application framework, providing a robust set of features for building single
and multi-page, and hybrid web applications. Yoke provides a thin layer of features fundamental to any web application,
without obscuring features that you know and love in Vert.x.

Before you start, you need to understand the basic functionality under Yoke. Yoke is a chain executor, modelled in a way
like the [chain-of-responsability pattern](http://en.wikipedia.org/wiki/Chain-of-responsibility_pattern). Yoke is
responsible to execute all your processing objects, which from now on we call Middleware. Middleware makes it easier for
software developers to perform specialized tasks, such as *HTTP Parsing*, *Authentication*, *Routing*, etc..., so they
can focus on the specific purpose of their application.

The API of Yoke is quite simple, in fact it consist only of 4 methods:

* ```use```
* ```set```
* ```engine```
* ```listen```

### use

You register middleware onto it using the ```use``` method, you might call the method with the following signatures:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
// Adds a Middleware (either from Yoke Middleware collection or your own to the chain.
// This middleware will run on every request path
use(Middleware middlewareImplementation);

// Adds a Middleware (either from Yoke Middleware collection or your own to the chain.
// This middleware will run only on requests which path starts with the route you specify. A tipical use case is when
// you want to do authentication on a specific route e.g.: all requests starting with /secure
use(String route, Middleware middlewareImplementation);

// Alternatively you can use handlers in the same way you would on Vert.x, internally these Handlers are handled as
// Middleware classes and processed accordingly.
use(Handler<YokeRequest> request);
use(String route, Handler<YokeRequest> request);
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

You can registeras many middleware as you want, but keep in mind that the order you used to register is the order that
is used to process a request. In other words If you need to consume the request body of a request you need to use the
[BodyParser](BodyParser.html) middleware before the middleware you are implementing.

The difference between the Middleware abstract class and the
[chain-of-responsability pattern](http://en.wikipedia.org/wiki/Chain-of-responsibility_pattern) is that you don't have
to define the "next" on the middleware itself but yoke takes care of it. An implementation of Middleware requires you
to implement the following method:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
/**
 * Handles a request.
 *
 * @param request A YokeRequest which in practice is a extended HttpServerRequest
 * @param next The callback to inform that the next middleware in the chain should be used. A value different from
 *             null represents an error and in that case the error handler middleware will be executed.
 */
public void handle(YokeRequest request, Handler<Object> next);
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The ```next``` parameter is a [callback](http://en.wikipedia.org/wiki/Callback_%28computer_programming%29). The reason
for using callbacks is to allow asynchronous execution of tasks. So in your code you can call ```next.handle(object)```
in the end of your method, or if you need to wait for some asynchronous call then at that moment you can call the
callback.

The value you pass to the callback is an *Error*. It might feel strange that the callback accepts *Object* instead of
*Exception* but the reason is that sometimes you might want to raise errors that do not require a full stack trace to be
generated by the JVM. In those cases you just pass a *Number* which is interpreted as a
[*HTTP Error*](http://en.wikipedia.org/wiki/List_of_HTTP_status_codes). When a *String* is passed, then a generic
*Internal Server Error* is raised. If your middleware completed successfuly then your error should be *null*.

To signal the end of processing of a request, just don't call the callback handler anymore. To summarize, calling next
informs Yoke to execute the next *Middleware* in the chain, if the handler contains anything other than *null* then an
error middleware is executed skipping all the remaining middleware.

### set

Every YokeRequest contains a context. A context is just a Java
[Map](http://docs.oracle.com/javase/7/docs/api/java/util/Map.html). The context is how you can share data between
middlewares and lives during the request lifetime. When a request arrives at Yoke the context is initialized with an
empty Map and all the values you define on Yoke with the ```set``` method. Yoke always defines the key *title* with the
value *Yoke*. You might want to override this by setting the title to your application name.

### engine

Yoke allows you to use template engines so you do not need to generate *HTML* by appending strings. You register an
engine but registering it's extension and a class that extends the Engine abstract class. By default Yoke bundles a
simple *StringPlaceholderEngine* that allows replacing keys in a document.

Of course this is a very naive engine so you can add your own or use other ones provided by the community. If you need
to implement one yourself all you need to implement is the following method:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
/**
 * The required to implement method.
 *
 * @param filename - String representing the file path to the template
 * @param context - Map with key values that might get substituted in the template
 * @param handler - The future result handler with a Buffer in case of success
 */
public void render(String filename, Map<String, Object> context, AsyncResultHandler<String> handler);
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The context is passed from the request directly and in fact is the same Map that was described on the ```set``` method.

To use the engines all you need to do is use the ```render``` method on the YokeResponse and pass the filename with the
correct file extension used to register the engine.

### listen

With this method you start a server, either listening on a specific port, address + port, or on top of an existing
Vert.x HttpServer.


## Hello World

At this moment you should understand the basic API of Yoke, and how it works so it is time to write your first *Hello
World* example.

Assuming you already bootstraped your project and are inside the working directory we are going to create the class:
```com.jetdrone.yoke.HelloWorldVerticle``` under ```src/main/java```. At this moment you should have the file
```src/main/java/com/jetdrone/yoke/HelloWorldVerticle.java``` in your file system.

The reason we have a *Verticle* class is because Yoke does not forces you to use any special classes, you still code
like you would on Vert.x. So we start by creating a Verticle the same way we would on any other Vert.x application:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines}
package com.jetdrone.vertx;

import com.jetdrone.vertx.yoke.*;
import com.jetdrone.vertx.yoke.middleware.*;

import org.vertx.java.core.*;
import org.vertx.java.platform.*;

public class HelloWorldVerticle extends Verticle {

  @Override
  public void start() {
  }
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This is the bare minimal Verticle code you need to write. Of course this code does not do anything fancy, in fact it
just do nothing. So lets make a *HTTP* web server that listens on port *8080*.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines startFrom="10"}
...
  public void start() {
    Yoke yoke = new Yoke(vertx);
    yoke.listen(8080);
  }
...
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

At this point you have almost everything needed to run your verticle, the only missing item is the module descriptor.
The module descriptor is a simple *JSON* file that describes the main verticle and any other metadata required for your
verticle.

For this example we only need to define 2 entries, the *main* verticle class and the dependency on *Yoke*. Create a file
under ```src/main/resources/mod.json``` with the following content:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.javascript .numberLines}
{
  "main": "com.jetdrone.vertx.HelloWorldVerticle",
  "includes": "com.jetdrone~yoke~1.0.0-SNAPSHOT"
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

And you can quickly test your new Verticle by running:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
pmlopes@jetdrone:~/Projects/yoke-tutorial$ ./gradlew runMod
pmlopes@jetdrone:~/Projects/yoke-tutorial$
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If you open a web browser and navigate to your localhost at port 8080 you will get a *404* *Not Found* error message.
This is expected since you have a server running but no middleware is handling your requests. So lets install a handler
for all request paths to respond with "Hello World".

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines startFrom="10"}
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
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If you restart your verticle running the gradle command from before and reload your web browser you will see the text
"Hello World" on it.


## REST

At this point you already know about the basics of Yoke, so lets extend the example and make a REST web service. Our
REST web service will listen on the path */resource*. Lets define a couple of functionality, when you *POST* a JSON
document, the document will be stored in a database, if you *PUT* the document will be updated, if you *GET* then you
retreive the document from the database and finally if you *DELETE* you delete the document from the database. Since
Database handling is not the purpose of Yoke we will assume data is always available.

Having to write code to handle all these *HTTP* verbs and paths would be to much boiler plate code. To help out Yoke
has a [Router middleware](Router.html). So lets replace the *Hello World" handler and replace it with the router
middleware. With this middleware we define the 4 actions *POST*, *PUT*, *GET* and *DELETE*.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines startFrom="10"}
...
  public void start() {
    Yoke yoke = new Yoke(vertx);
    yoke.use(new Router()
        .post("/resource", new Handler<YokeRequest>() {
          @Override
          public void handle(YokeRequest request) {
            // will implement this later...
          }
        })
        .put("/resource", new Handler<YokeRequest>() {
          @Override
          public void handle(YokeRequest request) {
            // will implement this later...
          }
        })
        .get("/resource", new Handler<YokeRequest>() {
          @Override
          public void handle(YokeRequest request) {
            // will implement this later...
          }
        })
        .delete("/resource", new Handler<YokeRequest>() {
          @Override
          public void handle(YokeRequest request) {
            // will implement this later...
          }
        }));
    yoke.listen(8080);
  }
...
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

So your application can handle all those actions under */resource*. Lets fill the handlers to do something. Since we
have no database in the code we assume there is a database abstraction that implements the actions. So our code would
become:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines startFrom="10"}
...
  public void start() {
    Yoke yoke = new Yoke(vertx);
    yoke.use(new Router()
        .post("/resource", new Handler<YokeRequest>() {
          @Override
          public void handle(YokeRequest request) {
            JsonObject body = request.jsonBody();
            String id = Database.store(body);
            JsonObject response = new JsonObject().putString("id", id);
            request.response().end(response);
          }
        })
        .put("/resource/:id", new Handler<YokeRequest>() {
          @Override
          public void handle(YokeRequest request) {
            JsonObject body = request.jsonBody();
            Database.update(request.params().get("id"), body);
            request.response().end(204);
          }
        })
        .get("/resource/:id", new Handler<YokeRequest>() {
          @Override
          public void handle(YokeRequest request) {
            JsonObject fromDb = Database.get(request.params().get("id"));
            request.response().end(fromDb);
          }
        })
        .delete("/resource/:id", new Handler<YokeRequest>() {
          @Override
          public void handle(YokeRequest request) {
            Database.delete(request.params().get("id"));
            request.response().end(204);
          }
        }));
    yoke.listen(8080);
  }
...
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Everything seems to be OK, however if you upload a json document nothing will show up on the body of your request. This
is expected because no one has parsed the request body. You don't need to worry about it, Yoke has a Middleware that
does that for you. You can install the [BodyParser](BodyParser.html) middleware easily. As it was described before, you
can use as many middleware as you want but the order matters. This means that if you need to have the body parsed in
your router middleware you are going to have to install the body parser before that, like this:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines startFrom="10"}
...
  public void start() {
    Yoke yoke = new Yoke(vertx);
    yoke.use(new BodyParser());
    yoke.use(new Router()
        .post("/resource", new Handler<YokeRequest>() {
...
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

So you now have a valid request body for your *POST* and *PUT*. This increases the developer productivity since you do
not need to spend time implementing a HTTP body parser. However having this on could lead your application to be
vulnerable to DoS attacks when someone would try to upload a json document bigger than your server memory. To be safer
you can use the [Limit](Limit.html) middleware which defines a maximum allowed size for your uploads. Again if you want
the application to be aware of the limit, this needs to be defined before the parser starts doing its work like this for
an upload of a maximum size of 4kb (4096 bytes):

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines startFrom="10"}
...
  public void start() {
    Yoke yoke = new Yoke(vertx);
    yoke.use(new Limit(4096));
    yoke.use(new BodyParser());
    yoke.use(new Router()
        .post("/resource", new Handler<YokeRequest>() {
...
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

You can look to the list of provided middleware and add as you like and also look at the 3rd party list.


## Error Handling

You have a working REST application but if an error occurs you need to handle it manually, however Yoke has a better way
for you. There is the possibility to use [ErrorHandler](ErrorHandler.html) middleware to generate pretty errors, however
by itself this is not enough, you need to inform Yoke you want it to handle the error for you. To do this we need to
change out handlers to become middlewares, lets focus alone in the *POST* code, since the other code would be the same.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines startFrom="10"}
...
  public void start() {
    Yoke yoke = new Yoke(vertx);
    yoke.use(new Limit(4096));
    yoke.use(new BodyParser());
    yoke.use(new Router()
        .post("/resource", new Middleware() {
          @Override
          public void handle(YokeRequest request, Handler<Object> next) {
            try {
              JsonObject body = request.jsonBody();
              String id = Database.store(body);
              JsonObject response = new JsonObject().putString("id", id);
              request.response().end(response);
            } catch (Exception e) {
              next.handle(e);
            }
          }
        })
...
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

So if there was an error Yoke will handle it and generate an error message. By default it will be a ugly text string
with the error name. To make it prettier lets use the error handler middleware.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines startFrom="10"}
...
  public void start() {
    Yoke yoke = new Yoke(vertx);
    yoke.use(new ErrorHandler(true));
    yoke.use(new Limit(4096));
    yoke.use(new BodyParser());
    yoke.use(new Router()
        .post("/resource", new Middleware() {
          @Override
          public void handle(YokeRequest request, Handler<Object> next) {
            try {
              JsonObject body = request.jsonBody();
              String id = Database.store(body);
              JsonObject response = new JsonObject().putString("id", id);
              request.response().end(response);
            } catch (Exception e) {
              next.handle(e);
            }
          }
        })
...
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Now your errors are pretty and include stack traces if present because you are creating an handler with 1st argument to
true.


## Templates

Not all applications are REST based and you might need to generate some HTML. It is not productive to inline the HTML in
strings in your code and concatenate them at request time. Yoke ships with a simple engine but more can be added as
explained before.

So lets use the built in [StringPlaceholderEngine](StringPlaceholderEngine.html) and use the template:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Hello ${name}!
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

So in order to do this, you first need to register the engine:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines startFrom="10"}
...
  public void start() {
    Yoke yoke = new Yoke(vertx);
    yoke.engine("tmpl", new StringPlaceholderEngine());
    yoke.use(new ErrorHandler(true));
...
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

And now in your Middleware/Handler you only need to call the render method. In order to fill the property *name* that
you can see in the template, you need to add it to the request context. As explained before the context is a Map and you
can update it by invoking the ```put``` method in the request.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
...
@Override
public void handle(YokeRequest request) {
  request.put("name", "Yoke World");
  request.response().render("mytemplate.tmpl");
}
...
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
