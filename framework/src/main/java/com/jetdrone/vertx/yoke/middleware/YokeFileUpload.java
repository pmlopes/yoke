// Copyright 2011-2013 the original author or authors.
//
// @package com.jetdrone.vertx.yoke.middleware
package com.jetdrone.vertx.yoke.middleware;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.http.HttpServerFileUpload;

import java.io.File;
import java.nio.charset.Charset;
import java.util.UUID;

// # YokeFileUpload
public class YokeFileUpload {

    final FileSystem fileSystem;

    private final String filename;
    private final String name;
    private final String contentType;
    private final String contentTransferEncoding;
    private final Charset charset;
    private final long size;
    private final String path;

    YokeFileUpload(Vertx vertx, HttpServerFileUpload fileUpload, String uploadDir) {
        this.fileSystem = vertx.fileSystem();

        this.filename = fileUpload.filename();
        this.name = fileUpload.name();
        this.contentType = fileUpload.contentType();
        this.contentTransferEncoding = fileUpload.contentTransferEncoding();
        this.charset = fileUpload.charset();
        this.size = fileUpload.size();

        if (!uploadDir.endsWith(File.separator)) {
            uploadDir += File.separator;
        }

        this.path = uploadDir + UUID.randomUUID().toString();
    }

    YokeFileUpload(FileSystem fileSystem, String filename, String name, String contentType, String contentTransferEncoding, Charset charset, long size, String path) {
        this.fileSystem = fileSystem;
        this.filename = filename;
        this.name = name;
        this.contentType = contentType;
        this.contentTransferEncoding = contentTransferEncoding;
        this.charset = charset;
        this.size = size;
        this.path = path;
    }

    /**
     * Returns the filename which was used when upload the file.
     */
    public String filename() {
        return filename;
    }

    /**
     * Returns the name of the attribute
     */
    public String name() {
        return name;
    }

    /**
     * Returns the contentType for the upload
     */
    public String contentType() {
        return contentType;
    }

    /**
     * Returns the contentTransferEncoding for the upload
     */
    public String contentTransferEncoding() {
        return contentTransferEncoding;
    }

    /**
     * Returns the charset for the upload
     */
    public Charset charset() {
        return charset;
    }

    /**
     * Returns the size of the upload (in bytes)
     */
    public long size() {
        return size;
    }

    /**
     * Returns filesystem path location
     */
    public String path() {
        return path;
    }

    public void delete(final Handler<Throwable> handler) {
        fileSystem.delete(path, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                    handler.handle(result.cause());
                } else {
                    handler.handle(null);
                }
            }
        });
    }

    public void delete() {
        this.delete(new Handler<Throwable>() {
            @Override
            public void handle(Throwable result) {
            }
        });
    }
}
