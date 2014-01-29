/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;

import java.util.Map;

/**
 * # Engine
 *
 * Engine represents a Template Engine that can be registered with Yoke. Any template engine just needs to implement
 * this template. The class provides access to the Vertx object so the engine might do I/O operations in the context of
 * the module.
 */
public interface Engine {

    /**
     * Called on register to allow using vertx in the engine.
     *
     * @param vertx Vertx instance
     */
    void setVertx(Vertx vertx);

    /**
     * Returns the default content type for this template, e.g.: `text/html`.
     *
     * @return the content type
     */
    String contentType();

    /**
     * Returns the default content encoding for this template, e.g.: `UTF-8`.
     *
     * @return content encoding
     */
    String contentEncoding();

    /**
     * The implementation of the render engine. The implementation should render the given file with the context in an
     * asynchronous way.
     *
     * @param filename String representing the file path to the template
     * @param context  Map with key values that might get substituted in the template
     * @param handler  The future result handler with a Buffer in case of success
     */
    void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> handler);
}
