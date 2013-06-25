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

// utilities

function wrapAsyncResultHandler(callback) {
    return new org.vertx.java.core.AsyncResultHandler({
        handle: callback
    });
}

function wrapAsyncResult(ex, result) {
    return new com.jetdrone.vertx.yoke.util.YokeAsyncResult(ex, result);
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
    return template(wrapMap(context));
};

module.exports = MicroTemplateEngine;
