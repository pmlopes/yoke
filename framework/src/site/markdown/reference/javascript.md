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


## Middleware

Middleware is the unit of work for Yoke, a ```Middleware``` is simply a class that implements the ```Middleware```
interface. The interface defines the following methods:

* ```handle(YokeRequest request Handler<Object> next)``` - This is the main method that needs to be implemented. The
  input is the current request wrapped in the class ```YokeRequest``` and a callback handler ```next```.

There are some extra methods available as helpers to get direct access to ```Vert.x```, ```FileSystem```,
```YokeSecurity``` and do some runtime initialization. By default Middleware registered directly to Yoke will run in the
same execution run of the request unless a asynchronous call is made in between, Middleware registered to other
Middleware containers such as ```Router``` is run in a asynchronous way. This last bit is quite important for some
Middleware such as ```BodyParser``` which needs to setup the request and Vert.x to parse the data as soon as possible.


## Engine

Engine represents a Template Engine that can be registered with Yoke. Any template engine just needs to implement this
template. The class provides access to the Vertx object so the engine might do I/O operations in the context of the
module.

Variables are passed to the engine from 2 sources:

* ```globals``` - globals are set on yoke itself using the ```set(String, Object)``` method.
* ```context``` - context variables are added to the request itself. The ```YokeRequest``` object contains the methods:
    * ```put(String, Object)``` - puts a value in the context (which can shadow global context values)
    * ```get(String)``` - gets a value from the context and when not found can get them from the global context
    
The base framework contains a simple string placeholder engine that recognizes variables using the format:
```${variable}```. Variable also allow composition for more complex scenarios: such as: ```${my${var}}```.

Of course this is a very simple and naive engine suitable for very simple situations, however if you need something more
powerful, you can use:

* ```Jade4JEngine``` - Use the [jade-lang](http://jade-lang.com/) template language.
* ```HandlebarsEngine``` - Use the [handlebars.js](http://handlebarsjs.com/) template language.
* ```MVEL``` - Use the [MVEL](https://github.com/mvel/mvel) template language.
* ```thymeleaf``` - *EXPERIMENTAL* Use the [thmeleaf](http://www.thymeleaf.org/) template language. 


## Store

Sometimes there is a need to store data across your cluster and you don't need to specify a database connection, a use
case for storage engines are session stores where the session data is stored and needs to be in sync across all
instances of your Vert.x app. The simplest store engine is ```SharedDataSessionStore```, this store uses the native
Vert.x ```SharedData``` class to handle all the storage related tasks. However ```SharedData``` has the limitation of
not synchronizing across the Vert.x cluster, for this you need to use either:

* ```RedisSessionStore``` - Requires a Redis server in your network plus the redis module.
* ```MongoDBSessionStore``` - Requires a MongoDB server in your network plus the mongo connector module.


## Bootstrap a Java Project

The easiest way to get started with Yoke is to bootstrap a project. Yoke support project skeletons using
[Grade](http://www.gradle.org) 1.x. In order to use this feature you need to download the jar file from maven manually
for the first time [yoke-2.0.4.jar](https://repo1.maven.org/maven2/com/jetdrone/yoke/2.0.4/yoke-2.0.4.jar). Once you
have this file in your computer bootstraping a java project is as simple as:

    java -jar yoke-2.0.4.jar --java <group>:<artifact>:<version>

Where ```group``` is usually your reverse domain name, ```artifact``` your project name and ```version``` the version of
your project. For example:

    java -jar yoke-2.0.4.jar --java com.jetdrone:hello:1.0
    
Will result in the following:

       create : hello
       create : hello/README.md
       create : hello/gradle.properties
       create : hello/conf.json
       create : hello/LICENSE.txt
       create : hello/gradlew.bat
       create : hello/gradlew
       create : hello/build.gradle
       create : hello/gradle
       create : hello/gradle/vertx.gradle
       create : hello/gradle/setup.gradle
       create : hello/gradle/maven.gradle
       create : hello/gradle/wrapper
       create : hello/gradle/wrapper/gradle-wrapper.properties
       create : hello/gradle/wrapper/gradle-wrapper.jar
       create : hello/src/main/java
       create : hello/src/main/java/com/jetdrone/hello
       create : hello/src/main/java/com/jetdrone/hello/App.java
       create : hello/src/main/resources
       create : hello/src/main/resources/public
       create : hello/src/main/resources/public/stylesheets
       create : hello/src/main/resources/public/stylesheets/style.css
       create : hello/src/main/resources/views
       create : hello/src/main/resources/views/index.shtml
       create : hello/src/main/resources/mod.json
       create : hello/src/test/java
       create : hello/src/test/java/com/jetdrone/hello
       create : hello/src/test/java/com/jetdrone/hello/AppTest.java
       create : hello/src/test/resources
    
    Go to your new app:
      cd hello
    
    Compile your app:
      ./gradlew build
    
    Run your app:
      ./gradlew runMod
    

## Step by Step simple App

At this moment you should have the file ```src/main/java/com/jetdrone/hello/App.java``` in your file system. A close
inspection to the generated code shows that you have *Verticle* class. The reason we have a *Verticle* class is because
Yoke does not forces you to use any special classes, you still code like you would on Vert.x.
 
The generated code enables the StringPlaceholderEngine and then enables some middleware to show what you can do. If you
want to start from scratch, of course you can just start with a simple Verticle.

So we start by creating a Verticle the same way we would on any other Vert.x application:

    package com.jetdrone.hello;

    import com.jetdrone.vertx.yoke.*;
    import com.jetdrone.vertx.yoke.middleware.*;

    import org.vertx.java.core.*;
    import org.vertx.java.platform.*;

    public class App extends Verticle {
      public void start() {
        ...
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
      "main": "com.jetdrone.hello.App",
      "includes": "com.jetdrone~yoke~2.0.4"
    }

And you can quickly test your new Verticle by running:

    $ ./gradlew runMod

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


## Routing

More complex routing can be acomplished with the ```Router``` Middleware. This middleware has a method for each HTTP
verb where you can register either a ```Pattern``` or a ```String``` path and a Middleware implementation.

* ```GET```
* ```PUT```
* ```POST```
* ```DELETE```
* ```OPTIONS```
* ```HEAD```
* ```TRACE```
* ```CONNECT```
* ```PATCH```

When you register a ```Pattern``` if a request ```path``` matches then the middleware is executed. When you use the
```String``` format some extra helpers are available. The ```String``` format is of the form: ```/path/:variable```.
When you use the style ```:variable``` some bootstrap is done behind the scenes where a regular expression is set to
catch the value, this requires that the variable value follows the format: ```:([A-Za-z][A-Za-z0-9_]*)```.

Behind the scenes the ```Router``` middleware will also catch the value from the variable and set it on the
```Context``` so you can access it using ```request.get("variable")```.

Requests can be validated using the ```Router``` ```param()``` method. If a param is registered and matches the variable
name from a HTTP method the validation is executed before the Middleware is called, and in case of failure an default
error ```400``` (Bad Request) is returned.


## Testing

The generated code also created a simple JUnit test. Yoke includes a ```YokeTester``` class that helps you write tests
without having to fully bootstrap a server, this makes testing your APIs simple as calling a method and inspect the
generated result.


    public class AppTest extends TestVerticle {
     
      @Test
      public void testApp() {
        final Yoke yoke = new Yoke(this);
        yoke.use(new Middleware() {
          @Override
          public void handle(YokeRequest request, Handler<Object> next) {
            request.response().end("OK");
          }
        });
     
        final YokeTester yokeAssert = new YokeTester(yoke);
     
        yokeAssert.request("GET", "/", new Handler<Response>() {
          @Override
          public void handle(Response response) {
            assertEquals("OK", response.body.toString());
            testComplete();
          }
        });
      }
    }
