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
package com.jetdrone.vertx.yoke.middleware;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerFileUpload;

import java.nio.charset.Charset;

public class YokeFileUpload {

    private final String filename;
    private final String name;
    private final String contentType;
    private final String contentTransferEncoding;
    private final Charset charset;
    private final long size;
    private final Buffer file;
    private Handler<Buffer> handler;
    private boolean complete;

    YokeFileUpload(HttpServerFileUpload fileUpload) {
        this.filename = fileUpload.filename();
        this.name = fileUpload.name();
        this.contentType = fileUpload.contentType();
        this.contentTransferEncoding = fileUpload.contentTransferEncoding();
        this.charset = fileUpload.charset();
        this.size = fileUpload.size();

        this.file = new Buffer();

        fileUpload.dataHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer buffer) {
                file.appendBuffer(buffer);
            }
        });

        fileUpload.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                complete = true;
                if (handler != null) {
                    handler.handle(file);
                }
            }
        });
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
     * Lazy handler of the upload body. Required since yoke does not guarantees when the callback is setup.
     */
    public void fileHandler(Handler<Buffer> handler) {
        this.handler = handler;
        if (complete) {
            handler.handle(file);
        }
    }
}
