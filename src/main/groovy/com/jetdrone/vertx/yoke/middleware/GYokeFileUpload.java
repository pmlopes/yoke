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

import groovy.lang.Closure;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

import java.nio.charset.Charset;

public class GYokeFileUpload extends YokeFileUpload {

    GYokeFileUpload(YokeFileUpload fileUpload) {
        super(fileUpload.fileSystem, fileUpload.filename(), fileUpload.name(), fileUpload.contentType(), fileUpload.contentTransferEncoding(), fileUpload.charset(), fileUpload.size(), fileUpload.path());
    }

    /**
     * Returns the filename which was used when upload the file.
     */
    public String getFilename() {
        return filename();
    }

    /**
     * Returns the name of the attribute
     */
    public String getName() {
        return name();
    }

    /**
     * Returns the contentType for the upload
     */
    public String getContentType() {
        return contentType();
    }

    /**
     * Returns the contentTransferEncoding for the upload
     */
    public String getContentTransferEncoding() {
        return contentTransferEncoding();
    }

    /**
     * Returns the charset for the upload
     */
    public Charset getCharset() {
        return charset();
    }

    /**
     * Returns the size of the upload (in bytes)
     */
    public long getSize() {
        return size();
    }

    /**
     * Returns filesystem path location
     */
    public String getPath() {
        return path();
    }

    public void delete(final Closure closure) {
        fileSystem.delete(path(), new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
                closure.call(result.cause());
            }
        });
    }
}
