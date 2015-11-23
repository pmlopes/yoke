/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware.filters;

import org.jetbrains.annotations.NotNull;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * # AbstractWriterFilter
 */
public abstract class AbstractWriterFilter implements WriterFilter {
    final Pattern filter;
    final OutputStream stream;
    final Buffer buffer = Buffer.buffer();

    public AbstractWriterFilter(@NotNull final Pattern filter) throws IOException {
        this.filter = filter;
        this.stream = createOutputStream();
    }

    private void write(byte[] b) {
        try {
            stream.write(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract OutputStream createOutputStream() throws IOException;

    private void end(byte[] b) {
        try {
            stream.write(b);
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(@NotNull final Buffer buffer) {
        write(buffer.getBytes());
    }

    @Override
    public void write(@NotNull final String chunk) {
        write(chunk.getBytes());
    }

    @Override
    public void write(@NotNull final String chunk, @NotNull final String enc) {
        write(chunk.getBytes(Charset.forName(enc)));
    }

    @Override
    public Buffer end(@NotNull final Buffer buffer) {
        end(buffer.getBytes());
        return this.buffer;
    }

    @Override
    public Buffer end(@NotNull final String chunk) {
        end(chunk.getBytes());
        return buffer;
    }

    @Override
    public Buffer end(@NotNull final String chunk, @NotNull final String enc) {
        end(chunk.getBytes(Charset.forName(enc)));
        return buffer;
    }

    @Override
    public boolean canFilter(@NotNull final String contentType) {
        return filter.matcher(contentType).find();
    }
}
