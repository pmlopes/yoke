# Yoke

Yoke is a middleware framework for Vert.x, shipping with over 12 bundled middleware.

[![Build Status](https://travis-ci.org/pmlopes/yoke.png?branch=master)](https://travis-ci.org/pmlopes/yoke)



## Instalation

To get started with Yoke in your project you can use Vert.x module system to download it from the maven repository or
include the maven dependency your self.


### Vert.x module id

At the moment this module is only compatible with Vert.x 2. It can be backported to the 1.x series but there is no work
in progress in that direction.

The Vert.x module id is: `com.jetdrone~yoke~1.0.0-SNAPSHOT`


### Maven artifact

You can get the jar file using maven with the following configuration:

    <dependency>
      <groupId>com.jetdrone</groupId>
      <artifactId>yoke</artifactId>
      <version>1.0.0-SNAPSHOT</version>
      <scope>provided</scope>
    </dependency>

The scope is provided because you should include the module from your Vert.x application to avoid having duplicated jars
in the server classpath.


## Where to get help

Please visit:  [yoke](http://pmlopes.github.io/yoke/)

In the site there are simple examples and basic documentation of the implemented middleware.

## Example Application

Under the directory example you will find a Naive implementation of a CMS application using Redis as a database backend.
The example shows how to use the following middleware:

* Favicon
* ErrorHandler
* Router
* BodyParser
* Static
* BasicAuth

And how to add custom middleware in the route to add extra information to the request.


## Inspiration

This project was inspired by [Connect](http://www.senchalabs.org/connect/).


## License

This project is released under the Apache License 2.0 as included in the root of the project.
