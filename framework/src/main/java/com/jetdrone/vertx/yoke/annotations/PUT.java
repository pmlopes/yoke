// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.annotations
package com.jetdrone.vertx.yoke.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// # PUT
//
// Annotate a method that will handle the HTTP PUT method requests for a given path.
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface PUT {
}
