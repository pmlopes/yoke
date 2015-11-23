/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import io.vertx.core.http.HttpClientOptions;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.VoidHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;

/** # RequestProxy
 *
 * RequestProxy provides web client a simple way to interact with other REST service
 * providers via Yoke, meanwhile Yoke could pre-handle authentication, logging and etc.
 *
 * In order to handler the proxy request properly, Bodyparser should be disabled for the
 * path matched by RequestProxy.
 */
public class RequestProxy extends Middleware {

    private final String prefix;
    private final String host;
    private final int port;
    private final boolean secure;

    public RequestProxy(@NotNull final String prefix, @NotNull final String host, final int port, final boolean secure) {
        this.prefix = prefix;
        this.host = host;
        this.port = port;
        this.secure = secure;
    }

    public RequestProxy(@NotNull final String prefix, final int port, final boolean secure) {
        this(prefix, "localhost", port, secure);
    }

    @Override
    public void handle(@NotNull final YokeRequest req, @NotNull final Handler<Object> next) {
        if (!req.uri().startsWith(prefix)) {
          next.handle(null);
          return;
        }
        final String newUri = req.uri().replaceFirst(prefix, "");
        final HttpClient client = vertx().createHttpClient(new HttpClientOptions().setDefaultHost(host).setDefaultPort(port).setSsl(secure));

        final HttpClientRequest cReq = client.request(req.method(), newUri, new Handler<HttpClientResponse>() {
          public void handle(HttpClientResponse cRes) {
            req.response().setStatusCode(cRes.statusCode());
            req.response().headers().setAll(cRes.headers());
            req.response().setChunked(true);
            cRes.handler(new Handler<Buffer>() {
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
        cReq.headers().setAll(req.headers());
        cReq.setChunked(true);
        req.handler(new Handler<Buffer>() {
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
