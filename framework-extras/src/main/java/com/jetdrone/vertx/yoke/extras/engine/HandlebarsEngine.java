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
package com.jetdrone.vertx.yoke.extras.engine;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import com.jetdrone.vertx.yoke.engine.AbstractEngineSync;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

import java.io.IOException;
import java.util.Map;

public class HandlebarsEngine extends AbstractEngineSync<Template> {

    private final Handlebars handlebars;

    public HandlebarsEngine(final String views, final String extension) {
        super(null);

        final String prefix = views.length() > 0 && views.endsWith("/") ? views : views + "/";

        handlebars = new Handlebars(new TemplateLoader() {
            @Override
            public TemplateSource sourceAt(final String location) throws IOException {
                try {
                    return new TemplateSource() {
                        @Override
                        public String content() throws IOException {
                            // load from the file system
                            return read(resolve(location));
                        }

                        @Override
                        public String filename() {
                            return location;
                        }

                        @Override
                        public long lastModified() {
                            return HandlebarsEngine.this.lastModified(location);
                        }
                    };
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IOException(e);
                }
            }

            @Override
            public String resolve(String location) {
                String normalized = normalize(location);
                if (normalized.endsWith(extension)) {
                    return prefix + normalized;
                }
                return prefix + normalized + extension;
            }

            protected String normalize(final String location) {
                if (location.startsWith("/")) {
                    return location.substring(1);
                }
                return location;
            }

            @Override
            public String getPrefix() {
                return prefix;
            }

            @Override
            public String getSuffix() {
                return extension;
            }
        });
    }

    @Override
    public void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> next) {
        try {
            Template template = getTemplateFromCache(filename);

            if (template == null) {
                // real compile
                template = handlebars.compile(filename);
                putTemplateToCache(filename, template);
            }

            next.handle(new YokeAsyncResult<>(new Buffer(template.apply(context))));
        } catch (IOException ex) {
            ex.printStackTrace();
            next.handle(new YokeAsyncResult<Buffer>(ex));
        }
    }

    public void render(final String filename, final String layoutFilename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> handler) {
        handler.handle(new YokeAsyncResult<Buffer>(new UnsupportedOperationException()));
    }

    public void registerHelper(String name, Helper<?> helper) {
        handlebars.registerHelper(name, helper);
    }
}
