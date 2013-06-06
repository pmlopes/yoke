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

import com.jetdrone.vertx.yoke.util.YokeAsyncResult;
import groovy.lang.MissingPropertyException;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.codehaus.groovy.control.CompilationFailedException;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class GroovyTemplateEngine extends AbstractEngine<Template> {

    private final TemplateEngine engine = new SimpleTemplateEngine();

    @Override
    public void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> next) {
        read(filename, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(new YokeAsyncResult<Buffer>(asyncResult.cause()));
                } else {
                    try {
                        Buffer result = internalRender(compile(filename, asyncResult.result()), context);
                        next.handle(new YokeAsyncResult<>(result));
                    } catch (CompilationFailedException | ClassNotFoundException | MissingPropertyException | IOException ex) {
                        next.handle(new YokeAsyncResult<Buffer>(ex));
                    }
                }
            }
        });
    }

    private Template compile(String filename, String templateText) throws IOException, ClassNotFoundException {
        Template template = getTemplateFromCache(filename);

        if (template == null) {
            // real compile
            template = engine.createTemplate(templateText);
            putTemplateToCache(filename, template);
        }

        return template;
    }

    private static Buffer internalRender(Template template, final Map<String, Object> context) throws IOException {
        final StringBuilder buffer = new StringBuilder();

        template.make(context).writeTo(new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                buffer.append(cbuf, off, len);
            }

            @Override
            public void flush() throws IOException {
                // noop
            }

            @Override
            public void close() throws IOException {
                // noop
            }
        });

        return new Buffer(buffer.toString());
    }
}
