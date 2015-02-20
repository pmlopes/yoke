/**
 * Copyright 2011-2014 the original author or authors.
 */

// utilities

function wrapAsyncResultHandler(callback) {
    return new org.vertx.java.core.AsyncResultHandler({
        handle: callback
    });
}

function wrapAsyncResult(ex, result) {
    return new com.jetdrone.vertx.yoke.core.YokeAsyncResult(ex, result);
}

function wrapMap(map) {
    var result = null;

    if (map !== null) {
        var it = map.entrySet().iterator();
        result = {};
        while (it.hasNext()) {
            var item = it.next();
            result[item.getKey()] = item.getValue();
        }
    }

    return result;
}

// real engine

function MicroTemplateEngine() {
    var self = this;
    this.jEngine = new com.jetdrone.vertx.yoke.engine.AbstractEngine({
        render: function (filename, context, handler) {
            self.jEngine.read(filename, wrapAsyncResultHandler(function (asyncResult) {
                if (asyncResult.failed()) {
                    handler.handle(wrapAsyncResult(asyncResult.cause()));
                } else {
                    try {
                        var result = self.render(self.compile(filename, asyncResult.result()), context);
                        handler.handle(wrapAsyncResult(null, result));
                    } catch (e) {
                        handler.handle(wrapAsyncResult(e, null));
                    }
                }
            }));
        },
        extension: function() {
            return ".ejs";
        }
    });
}

MicroTemplateEngine.prototype.compile = function (filename, templateText) {
    var self = this;
    var template = self.jEngine.getTemplateFromCache(filename);

    if (template == null) {
        // real compile
        // compile the template to a JS function
        template = new Function("obj",
            "var p=[],print=function(){p.push.apply(p,arguments);};" +

                // Introduce the data as local variables using with(){}
                "with(obj){p.push('" +

                // Convert the template into pure JavaScript
                templateText
                    .replace(/[\r\t\n]/g, " ")
                    .split("<%").join("\t")
                    .replace(/((^|%>)[^\t]*)'/g, "$1\r")
                    .replace(/\t=(.*?)%>/g, "',$1,'")
                    .split("\t").join("');")
                    .split("%>").join("p.push('")
                    .split("\r").join("\\'")
                + "');}return p.join('');");

        // store it in the java layer
        self.jEngine.putTemplateToCache(filename, template);
    }

    return template
};

MicroTemplateEngine.prototype.render = function (template, context) {
    return new org.vertx.java.core.buffer.Buffer(template(wrapMap(context)));
};

module.exports = MicroTemplateEngine;
