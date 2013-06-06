/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetdrone.vertx.yoke;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

import java.util.Map;

/**
 * Engine represents a Template Engine that can be registered with Yoke. Any template engine just needs to
 * extend this abstract class. The class provides access to the Vertx object so the engine might do I/O
 * operations in the context of the module.
 */
public interface Engine<T> {

    /**
     * Called on register to allow using vertx in the engine
     * @param vertx Vertx
     */
    void setVertx(Vertx vertx);

    /**
     * Returns the default content type for this template.
     * For example: text/html
     * @return String
     */
    String contentType();

    /**
     * Returns the default content encoding for this template.
     * For example: UTF-8
     * @return String
     */
    String contentEncoding();

    /**
     * The required to implement method.
     *
     * @param filename - String representing the file path to the template
     * @param context - Map with key values that might get substituted in the template
     * @param handler - The future result handler with a Buffer in case of success
     */
    public abstract void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<String>> handler);
}
