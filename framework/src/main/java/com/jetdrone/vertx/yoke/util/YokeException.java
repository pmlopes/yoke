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
package com.jetdrone.vertx.yoke.util;

import io.netty.handler.codec.http.HttpResponseStatus;

public class YokeException extends Throwable {

    private static final long serialVersionUID = 1L;

    private final Number code;

    public YokeException(Number code) {
        this(code, HttpResponseStatus.valueOf(code.intValue()).reasonPhrase());
    }

    public YokeException(Number code, String message) {
        super(message);
        this.code = code;
    }

    public YokeException(String message) {
        super(message);
        this.code = 500;
    }

    public YokeException(Number code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public Number getErrorCode() {
        return code;
    }
}
