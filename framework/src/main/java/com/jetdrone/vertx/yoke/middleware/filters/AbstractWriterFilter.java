package com.jetdrone.vertx.yoke.middleware.filters;

import org.vertx.java.core.buffer.Buffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

public abstract class AbstractWriterFilter implements WriterFilter {
    final Pattern filter;
    final OutputStream stream;
    final Buffer buffer = new Buffer();

    public AbstractWriterFilter(Pattern filter) throws IOException {
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
    public void write(Buffer buffer) {
        write(buffer.getBytes());
    }

    @Override
    public void write(String chunk) {
        write(chunk.getBytes());
    }

    @Override
    public void write(String chunk, String enc) {
        write(chunk.getBytes(Charset.forName(enc)));
    }

    @Override
    public Buffer end(Buffer buffer) {
        end(buffer.getBytes());
        return buffer;
    }

    @Override
    public Buffer end(String chunk) {
        end(chunk.getBytes());
        return buffer;
    }

    @Override
    public Buffer end(String chunk, String enc) {
        end(chunk.getBytes(Charset.forName(enc)));
        return buffer;
    }

    @Override
    public boolean canFilter(String contentType) {
        return filter.matcher(contentType).find();
    }
}
