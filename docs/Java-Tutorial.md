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
pmlopes@jetdrone:~/Projects$ **wget http://pmlopes.github.io/yoke/yoke-gradle-template.tar.gz**
DUMP HERE THE OUTPUT OF THE COMMAND
pmlopes@jetdrone:~/Projects$ **tar -zxvf yoke-gradle-template.tar.gz**
yoke-gradle-template/
yoke-gradle-template/README.md
yoke-gradle-template/src/
yoke-gradle-template/src/main/
yoke-gradle-template/src/main/resources/
yoke-gradle-template/src/main/resources/mod.json
yoke-gradle-template/src/test/
yoke-gradle-template/src/test/java/
yoke-gradle-template/src/test/java/com/
yoke-gradle-template/src/test/java/com/jetdrone/
yoke-gradle-template/src/test/java/com/jetdrone/vertx/
yoke-gradle-template/src/test/java/com/jetdrone/vertx/yoke/
yoke-gradle-template/src/test/java/com/jetdrone/vertx/yoke/test/
yoke-gradle-template/src/test/java/com/jetdrone/vertx/yoke/test/Response.java
yoke-gradle-template/src/test/java/com/jetdrone/vertx/yoke/test/YokeTester.java
yoke-gradle-template/gradlew.bat
yoke-gradle-template/build.gradle
yoke-gradle-template/gradlew
yoke-gradle-template/gradle/
yoke-gradle-template/gradle/maven.gradle
yoke-gradle-template/gradle/setup.gradle
yoke-gradle-template/gradle/vertx.gradle
yoke-gradle-template/gradle/wrapper/
yoke-gradle-template/gradle/wrapper/gradle-wrapper.properties
yoke-gradle-template/gradle/wrapper/gradle-wrapper.jar
yoke-gradle-template/gradle.properties
pmlopes@jetdrone:~/Projects$
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The extracted project lives under the directory *yoke-gradle-template* it should be expected to be renamed to a better
suitable name describing the project to be implemented.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
pmlopes@jetdrone:~/Projects$ **mv yoke-gradle-template yoke-tutorial**
pmlopes@jetdrone:~/Projects$ **cd yoke-tutorial**
pmlopes@jetdrone:~/Projects/yoke-tutorial$
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


## Creating the first Hello World

## Expose a REST operation

## Error Handling

## Templates
