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
package com.jetdrone.vertx.yoke.middleware.filters;

import org.vertx.java.core.buffer.Buffer;

public interface WriterFilter {
    /**
     * Returns the content encoding name for this filter.
     * @return encoder name gzip, deflate
     */
    String encoding();

    void write(Buffer buffer);

    void write(String chunk);

    void write(String chunk, String enc);

    Buffer end(Buffer buffer);

    Buffer end(String chunk);

    Buffer end(String chunk, String enc);

    boolean canFilter(String contentType);
}
