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

import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import com.jetdrone.vertx.yoke.engine.AbstractEngineSync;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

public class Jade4JEngine extends AbstractEngineSync<JadeTemplate> {

    private final JadeConfiguration config = new JadeConfiguration();

    private final String prefix;
    private final String extension;

    public Jade4JEngine(final String views, final String extension) {
        super(null);

        prefix = views.length() > 0 && views.endsWith("/") ? views : views + "/";
        this.extension = extension;

        config.setTemplateLoader(new TemplateLoader() {
            @Override
            public long getLastModified(String name) throws IOException {
                return Jade4JEngine.this.lastModified(name);
            }

            @Override
            public Reader getReader(String name) throws IOException {
                return new StringReader(read(resolve(name)));
            }
        });
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
    public void render(final String filename, final String layoutFilename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> handler) {
        handler.handle(new YokeAsyncResult<Buffer>(new UnsupportedOperationException()));
    }

    @Override
    public void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> next) {
        try {
            JadeTemplate template = getTemplateFromCache(resolve(filename));

            if (template == null) {
                // real compile
                template = config.getTemplate(resolve(filename)); // missing resolved path
                putTemplateToCache(resolve(filename), template); // missing resolved path
            }

            next.handle(new YokeAsyncResult<>(new Buffer(config.renderTemplate(template, context))));
        } catch (Exception ex) {
            ex.printStackTrace();
            next.handle(new YokeAsyncResult<Buffer>(ex));
        }
    }
}
