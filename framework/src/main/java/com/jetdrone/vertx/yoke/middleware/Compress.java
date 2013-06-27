package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

public class Compress extends Middleware {

    private static final class GZipWriterFilter implements WriterFilter {

        final GZIPOutputStream gzip;
        private Buffer buffer;

        GZipWriterFilter() {
            try {
                gzip = new GZIPOutputStream(new OutputStream() {
                    @Override
                    public void write(int i) throws IOException {
                        buffer.appendByte((byte) i);
                    }
                });
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }

        @Override
        public Buffer write(String contentType, Buffer string) {
            try {
                buffer = new Buffer();
                gzip.write(string.getBytes());
                gzip.flush();
                return buffer;
            } catch (IOException e) {
                return null;
            }
        }
    }

    private static final class DeflateWriterFilter implements WriterFilter {

        final DeflaterOutputStream deflate;
        private Buffer buffer;

        DeflateWriterFilter() {
            deflate = new DeflaterOutputStream(new OutputStream() {
                @Override
                public void write(int i) throws IOException {
                    buffer.appendByte((byte) i);
                }
            });
        }

        @Override
        public Buffer write(String contentType, Buffer string) {
            try {
                buffer = new Buffer();
                deflate.write(string.getBytes());
                deflate.flush();
                return buffer;
            } catch (IOException e) {
                return null;
            }
        }
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
