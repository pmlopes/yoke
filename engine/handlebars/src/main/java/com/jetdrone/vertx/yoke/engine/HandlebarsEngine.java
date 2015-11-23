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

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class HandlebarsEngine extends AbstractEngineSync<Template> {

    private final Handlebars handlebars;
    private final String prefix;
    private final String extension;

    public HandlebarsEngine(final String views) {
        this(views, ".hbs");
    }

    public HandlebarsEngine(final String views, final String extension) {
        this.extension = extension;

        if ("".equals(views)) {
            prefix = views;
        } else {
            prefix = views.endsWith("/") ? views : views + "/";
        }

        handlebars = new Handlebars(new TemplateLoader() {
            @Override
            public TemplateSource sourceAt(final String location) throws IOException {
                // load from the file system
                final String buffer = read(resolve(location));

                if (buffer == null) {
                    throw new FileNotFoundException(location);
                }

                return new TemplateSource() {
                    @Override
                    public String content() throws IOException {
                        // load from the file system
                        return buffer;
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
            }

            @Override
            public String resolve(String location) {
                return HandlebarsEngine.this.resolve(location);
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
    public String extension() {
        return extension;
    }

    private String resolve(String location) {
        String normalized = normalize(location);
        if (normalized.endsWith(extension)) {
            return prefix + normalized;
        }
        return prefix + normalized + extension;
    }

    private String normalize(final String location) {
        if (location.startsWith("/")) {
            return location.substring(1);
        }
        return location;
    }

    @Override
    public void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> next) {
        try {
            Template template = getTemplateFromCache(resolve(filename));

            if (template == null) {
                // real compile
                template = handlebars.compile(filename);
                putTemplateToCache(resolve(filename), template);
            }

            next.handle(new YokeAsyncResult<>(Buffer.buffer(template.apply(context))));
        } catch (Exception ex) {
            ex.printStackTrace();
            next.handle(new YokeAsyncResult<Buffer>(ex));
        }
    }

    public final Handlebars getHandlebars() {
        return handlebars;
    }
}
