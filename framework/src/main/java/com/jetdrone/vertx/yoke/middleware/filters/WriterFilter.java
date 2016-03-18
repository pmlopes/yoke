/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware.filters;

import io.vertx.core.buffer.Buffer;

/**
 * # WriterFilter
 */
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
