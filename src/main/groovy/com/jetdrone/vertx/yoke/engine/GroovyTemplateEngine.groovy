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
package com.jetdrone.vertx.yoke.engine

import com.jetdrone.vertx.yoke.Engine
import com.jetdrone.vertx.yoke.util.YokeAsyncResult
import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import groovy.text.TemplateEngine
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.Handler

@CompileStatic public class GroovyTemplateEngine extends Engine {

    private TemplateEngine engine = new SimpleTemplateEngine()

    @Override
    public void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<String>> next) {
        // verify if the file is still fresh in the cache
        isFresh(filename, new Handler<Boolean>() {
            @Override
            public void handle(Boolean fresh) {
                if (fresh) {
                    try {
                        String result = internalRender(compile(filename), context)
                        next.handle(new YokeAsyncResult<String>(result))
                    } catch (CompilationFailedException | ClassNotFoundException | MissingPropertyException | IOException ex) {
                        next.handle(new YokeAsyncResult<String>(ex))
                    }
                } else {
                    loadToCache(filename, new Handler<Throwable>() {
                        @Override
                        public void handle(final Throwable throwable) {
                            if (throwable != null) {
                                next.handle(new YokeAsyncResult<String>(throwable))
                            } else {
                                try {
                                    String result = internalRender(compile(filename), context)
                                    next.handle(new YokeAsyncResult<String>(result))
                                } catch (CompilationFailedException | ClassNotFoundException | MissingPropertyException | IOException ex) {
                                    next.handle(new YokeAsyncResult<String>(ex))
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private Template compile(String filename) {
        Template template = (Template) getTemplateFromCache(filename)

        if (template == null) {
            // real compile
            template = engine.createTemplate(getFileFromCache(filename))
            putTemplateToCache(filename, template)
        }

        return template
    }

    private static String internalRender(Template template, final Map<String, Object> context) {
        final StringBuilder buffer = new StringBuilder()

        template.make(context).writeTo(new Writer() {
            @Override
            void write(char[] cbuf, int off, int len) throws IOException {
                buffer.append(cbuf, off, len)
            }

            @Override
            void flush() throws IOException {
                // noop
            }

            @Override
            void close() throws IOException {
                // noop
            }
        })

        return buffer.toString()
    }
}
