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
import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import groovy.text.TemplateEngine
import org.codehaus.groovy.control.CompilationFailedException
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.core.buffer.Buffer

public class GroovyTemplateEngine extends Engine {

    private TemplateEngine engine = new SimpleTemplateEngine()

    @Override
    public void render(final String file, final Map<String, Object> context, final AsyncResultHandler<Buffer> next) {
        loadTemplate(file, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(new Engine.EngineAsyncResult<Buffer>(asyncResult.cause(), null))
                } else {
                    try {
                        Template template = engine.createTemplate(asyncResult.result())
                        // TODO: if not null then cache it

                        final Buffer buffer = new Buffer(0);

                        template.make(context).writeTo(new Writer() {
                            @Override
                            void write(char[] cbuf, int off, int len) throws IOException {
                                buffer.appendString(new String(cbuf, off, len))
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

                        next.handle(new Engine.EngineAsyncResult(null, buffer));

                    } catch (CompilationFailedException | ClassNotFoundException | MissingPropertyException | IOException ex) {
                        next.handle(new Engine.EngineAsyncResult<Buffer>(ex, null))
                    }
                }
            }
        });
    }
}
