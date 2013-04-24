package com.jetdrone.vertx.kitcms;

import com.jetdrone.vertx.kitcms.mustache.MustacheException;
import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.kitcms.mustache.Mustache;
import com.jetdrone.vertx.kitcms.mustache.Template;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.buffer.Buffer;

import java.util.Map;

public class MustacheEngine extends Engine {
    @Override
    public void render(final String template, final Map<String, Object> context, final AsyncResultHandler<Buffer> next) {

        vertx.fileSystem().readFile(template, new AsyncResultHandler<Buffer>() {
            @Override
            public void handle(AsyncResult<Buffer> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(asyncResult);
                } else {
                    final Template tmpl = Mustache.compiler().compile(asyncResult.result().toString("UTF-8"));
                    try {
                        final Buffer out = new Buffer(tmpl.execute(context));
                        next.handle(new FutureAsyncResult<>(null, out));
                    } catch (MustacheException me) {
                        next.handle(new FutureAsyncResult<Buffer>(me, null));
                    }
                }
            }
        });
    }
}
