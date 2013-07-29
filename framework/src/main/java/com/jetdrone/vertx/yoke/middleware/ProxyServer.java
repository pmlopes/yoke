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
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;

/**
* ProxyServer provides web client a simple way to interact with other REST service
* providers via Yoke, meanwhile Yoke could pre-handle authentication, logging and etc.<p>
*
* In order to handler the proxy request properly, Bodyparser should be disabled for the
* path matched by ProxyServer.<p>
*/
public class ProxyServer extends Middleware {

    private final String prefix;
    private final String host;
    private final int port;

    @Override
    public Middleware init(final Vertx vertx, final Logger logger) {
        super.init(vertx, logger);
        return this;
    }

    public ProxyServer(String prefix, String host, int port) {
        this.prefix = prefix;
        this.host = host;
        this.port = port;        
    }

    public ProxyServer(String prefix, int port) {
        this(prefix, "localhost", port);     
    }

    @Override
    public void handle(final YokeRequest req, final Handler<Object> next) {
        if (!req.uri().startsWith(prefix)) {
          next.handle(null);
          return;
        }
        final String newUri = req.uri().replaceFirst(prefix, "");
        final HttpClient client = vertx.createHttpClient().setHost(host).setPort(port);
        
        final HttpClientRequest cReq = client.request(req.method(), newUri, new Handler<HttpClientResponse>() {
          public void handle(HttpClientResponse cRes) {
            req.response().setStatusCode(cRes.statusCode());
            req.response().headers().set(cRes.headers());
            req.response().setChunked(true);
            cRes.dataHandler(new Handler<Buffer>() {
              public void handle(Buffer data) {
                req.response().write(data);
              }
            });
            cRes.endHandler(new VoidHandler() {
              public void handle() {
                req.response().end();
              }
            });
            cRes.exceptionHandler(new Handler<Throwable>() {
              public void handle(Throwable t) {
                next.handle(t);
              }
            });
          }
        });
        cReq.headers().set(req.headers());
        cReq.setChunked(true);
        req.dataHandler(new Handler<Buffer>() {
          public void handle(Buffer data) {
            cReq.write(data);
          }
        });
        req.endHandler(new VoidHandler() {
          public void handle() {
            cReq.end();
          }
        });
    }
}
