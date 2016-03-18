# Yoke

Yoke is a polyglot middleware framework for Vert.x, shipping with over 12 bundled middleware.

[![Build Status](https://travis-ci.org/pmlopes/yoke.png?branch=master)](https://travis-ci.org/pmlopes/yoke)


## Instalation

To get started with Yoke in your project you can use Vert.x module system to download it from the maven repository or
include the maven dependency your self. The moment this module is only compatible with Vert.x 2.

The Vert.x module id is: `com.jetdrone~yoke~3.0.0`. If you prefer to use [maven](http://maven.apache.org), you can get
the artifacts using the following dependency:

    <dependency>
      <groupId>com.jetdrone</groupId>
      <artifactId>yoke</artifactId>
      <version>3.0.0</version>
    </dependency>


## Getting started

Yoke 3 is a transitional package that you can use to port your existing apps to Vert.x3, however it is recommended to
use Vert.x-Web for new development since that is where the focus is now.

If you need help with Yoke. just ask your questions on [yoke framework group](https://groups.google.com/forum/#!forum/yoke-framework).


## Example Applications

Under the directory example you will find a couple of examples built with Yoke.

* A naive implementation of a CMS application using Redis as a database backend
* Mozilla Persona authentication implementation
* Groovy get started example
* JavaScript get started example

All these examples are presented as a showcase of the framework, they are lacking lots of features and testing.


## Inspiration

This project was inspired by [Connect](http://www.senchalabs.org/connect/).


## License

This project is released under the Apache License 2.0 as included in the root of the project.
