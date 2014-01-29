/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.engine;

import java.util.Map;

/**
 * # Function
 */
public interface Function {

    String exec(Map<String, Object> context, Object... args);
}
