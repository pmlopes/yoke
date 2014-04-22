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
package com.jetdrone.vertx.yoke.engine;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class ThymeleafEngine implements Engine {

    private final TemplateEngine engine;

    private final String prefix;
    private final String extension = ".html";

    public ThymeleafEngine(final String views) {
        if ("".equals(views)) {
            prefix = views;
        } else {
            prefix = views.endsWith("/") ? views : views + "/";
        }

        TemplateResolver templateResolver = new TemplateResolver();

        // XHTML is the default mode, but we will set it anyway for better understanding of code
        templateResolver.setTemplateMode("XHTML");
        // This will convert "home" to "/WEB-INF/templates/home.html"
        templateResolver.setPrefix(prefix);
        templateResolver.setSuffix(extension);
        // Set template cache TTL to 1 hour. If not set, entries would live in cache until expelled by LRU
        templateResolver.setCacheTTLMs(3600000L);

        engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
    }

    @Override
    public void setVertx(Vertx vertx) {
    }

    @Override
    public String contentType() {
        return null;
    }

    @Override
    public String contentEncoding() {
        return null;
    }

    @Override
    public String extension() {
        return extension;
    }

    @Override
    public void render(final String filename, final String layoutFilename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> handler) {
        handler.handle(new YokeAsyncResult<Buffer>(new UnsupportedOperationException()));
    }

    @Override
    public void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> next) {

        final Buffer buffer = new Buffer();

        try {
            // TODO: resolve filename
            // TODO: convert context to IContext
            engine.process(filename, (IContext) null, new Writer() {
                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    buffer.appendString(new String(cbuf, off, len));
                }

                @Override
                public void flush() throws IOException {}

                @Override
                public void close() throws IOException {}
            });

            next.handle(new YokeAsyncResult<>(buffer));
        } catch (Exception ex) {
            ex.printStackTrace();
            next.handle(new YokeAsyncResult<Buffer>(ex));
        }
    }
}
