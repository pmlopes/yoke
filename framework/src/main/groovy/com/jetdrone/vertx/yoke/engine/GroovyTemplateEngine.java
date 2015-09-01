/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.engine;

import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import groovy.lang.MissingPropertyException;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.codehaus.groovy.control.CompilationFailedException;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class GroovyTemplateEngine extends AbstractEngine<Template> {

    private final String extension;
    private final String prefix;

    public GroovyTemplateEngine(final String views) {
        this(views, ".gsp");
    }

    public GroovyTemplateEngine(final String views, final String extension) {
        this.extension = extension;

        if ("".equals(views)) {
            prefix = views;
        } else {
            prefix = views.endsWith("/") ? views : views + "/";
        }
    }

    private final TemplateEngine engine = new SimpleTemplateEngine();

    @Override
    public String extension() {
        return extension;
    }

    @Override
    public void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> next) {
        read(prefix + filename, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(new YokeAsyncResult<Buffer>(asyncResult.cause()));
                } else {
                    try {
                        Buffer result = internalRender(compile(prefix + filename, asyncResult.result()), context);
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
            public void write(@NotNull char[] cbuf, int off, int len) throws IOException {
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
