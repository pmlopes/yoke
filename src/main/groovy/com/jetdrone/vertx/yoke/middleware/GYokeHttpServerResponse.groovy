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
package com.jetdrone.vertx.yoke.middleware

import com.jetdrone.vertx.yoke.Engine
import org.vertx.java.core.http.HttpServerResponse
import org.vertx.groovy.core.buffer.Buffer;

public class GYokeHttpServerResponse extends YokeHttpServerResponse {

    GYokeHttpServerResponse(HttpServerResponse response, Map<String, Object> context, Map<String, Engine> engines) {
        super(response, context, engines)
    }

    public GYokeHttpServerResponse write(Buffer buffer) {
        write(buffer.toJavaBuffer());
        this;
    }

    public void end(Buffer buffer) {
        end(buffer.toJavaBuffer());
    }

    public GYokeHttpServerResponse leftShift(Buffer buffer) {
        write(buffer);
    }

    public GYokeHttpServerResponse leftShift(String s) {
        write(s);
        this;
    }
}
