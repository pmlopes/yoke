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

import com.jetdrone.vertx.yoke.engine.AbstractEngine;
import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

import java.io.IOException;
import java.util.Map;

public class MVELEngine extends AbstractEngine<CompiledTemplate> {

    public MVELEngine() {
        super();
    }

    public MVELEngine(String templateBodyKey) {
        super(templateBodyKey);
    }

    @Override
    public void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> next) {
        read(filename, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(new YokeAsyncResult<Buffer>(asyncResult.cause()));
                } else {
                    try {
                        CompiledTemplate template = compile(filename, asyncResult.result());
                        next.handle(new YokeAsyncResult<>(new Buffer((String) TemplateRuntime.execute(template, context))));
                    } catch (IOException ex) {
                        next.handle(new YokeAsyncResult<Buffer>(ex));
                    }
                }
            }
        });
    }

    private CompiledTemplate compile(String filename, String templateText) throws IOException {

        CompiledTemplate template = getTemplateFromCache(filename);

        if (template == null) {
            // real compile
            template = TemplateCompiler.compileTemplate(templateText);
            putTemplateToCache(filename, template);
        }

        return template;
    }
}
