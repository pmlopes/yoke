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
import org.thymeleaf.Arguments;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.VariablesMap;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.messageresolver.MessageResolution;
import org.thymeleaf.resourceresolver.IResourceResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

public class ThymeleafEngine implements Engine {

    private final TemplateEngine engine = new TemplateEngine();
    private final TemplateResolver templateResolver;

    private final String extension;

    public ThymeleafEngine(final String views) {
        this(views, ".html");
    }
    public ThymeleafEngine(final String views, final String extension) {
        this.extension = extension;

        String prefix;
        if ("".equals(views)) {
            prefix = views;
        } else {
            prefix = views.endsWith("/") ? views : views + "/";
        }

        templateResolver = new TemplateResolver();

        // XHTML is the default mode, but we will set it anyway for better understanding of code
        templateResolver.setTemplateMode("XHTML");
        templateResolver.setPrefix(prefix);
    }

    @Override
    public void setVertx(Vertx vertx) {
        final FileSystem fs = vertx.fileSystem();

        templateResolver.setResourceResolver(new IResourceResolver() {

            @Override
            public String getName() {
                return "Yoke/Thymeleaf";
            }

            @Override
            public InputStream getResourceAsStream(TemplateProcessingParameters templateProcessingParameters, String resourceName) {

                if (!fs.existsBlocking(resourceName)) {
                    return null;
                }

                final Buffer buffer = fs.readFileBlocking(resourceName);

                return new InputStream() {
                    int pos = 0;

                    @Override
                    public int read() throws IOException {
                        if (pos == buffer.length()) {
                            return -1;
                        }

                        return buffer.getByte(pos++);
                    }
                };
            }
        });

        engine.setTemplateResolver(templateResolver);
//        engine.setMessageResolver(new IMessageResolver() {
//            @Override
//            public String getName() {
//                return "Yoke/Thymeleaf";
//            }
//
//            @Override
//            public Integer getOrder() {
//                return 1;
//            }
//
//            @Override
//            public MessageResolution resolveMessage(Arguments arguments, String key, Object[] messageParameters) {
//                return null;
//            }
//
//            @Override
//            public void initialize() {
//
//            }
//        });
    }

    @Override
    public String contentType() {
        return "text/html";
    }

    @Override
    public String contentEncoding() {
        return "UTF-8";
    }

    @Override
    public String extension() {
        return extension;
    }

    @Override
    public void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> next) {

        final Buffer buffer = Buffer.buffer();

        try {
            engine.process(filename, toIContext(context), new Writer() {
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

    private IContext toIContext(final Map<String, Object> context) {

        final VariablesMap<String, Object> variables = new VariablesMap<>(context);

        return new IContext() {
            @Override
            public VariablesMap<String, Object> getVariables() {
                return variables;
            }

            @Override
            public Locale getLocale() {
                return Locale.getDefault();
            }

            @Override
            public void addContextExecutionInfo(String templateName) {

            }
        };
    }
}
