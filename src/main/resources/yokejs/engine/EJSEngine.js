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

function wrapHandler(callback) {
    return new org.vertx.java.core.Handler({
        handle: callback
    });
}

function wrapAsyncResult(ex, result) {
    return new com.jetdrone.vertx.yoke.util.YokeAsyncResult(ex, result);
}

// real engine

function EJSEngine() {
    var self = this;
    this.jEngine = new com.jetdrone.vertx.yoke.Engine({
        render: function (filename, context, handler) {
            self.jEngine.isFresh(filename, wrapHandler(function (fresh) {
                if (fresh) {
                    var template = self.jEngine.getFileFromCache(filename);
                    try {
                        handler.handle(wrapAsyncResult(null, template));
                    } catch (e) {
                        handler.handle(wrapAsyncResult(e, null));
                    }
                } else {
                    self.jEngine.load(filename, wrapHandler(function (asyncResult) {
                        if (asyncResult.failed()) {
                            handler.handle(wrapAsyncResult(asyncResult.cause()));
                        } else {
                            var template = asyncResult.result();
                            try {
                                handler.handle(wrapAsyncResult(null, template));
                            } catch (e) {
                                handler.handle(wrapAsyncResult(e, null));
                            }
                        }
                    }));
                }
            }));
        }
    });
}

module.exports = EJSEngine;
