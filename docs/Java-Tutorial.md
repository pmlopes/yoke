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
pmlopes@jetdrone:~/Projects$ **curl http://pmlopes.github.io/yoke/yoke-gradle-template.tar.gz | tar -zx**
pmlopes@jetdrone:~/Projects$
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The extracted project lives under the directory *yoke-gradle-template* it should be expected to be renamed to a better
suitable name describing the project to be implemented.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
pmlopes@jetdrone:~/Projects$ **mv yoke-gradle-template yoke-tutorial**
pmlopes@jetdrone:~/Projects$ **cd yoke-tutorial**
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

## REST

## Error Handling

## Templates
