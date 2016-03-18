/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package com.jetdrone.vertx.yoke.sockjs.impl;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.Router;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.sockjs.SockJSSocket;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RawWebSocketTransport {

  private static class RawWSSockJSSocket extends SockJSSocketBase {

    ServerWebSocket ws;
    MultiMap headers;

    RawWSSockJSSocket(Vertx vertx, ServerWebSocket ws) {
      super(vertx);
      this.ws = ws;
      ws.closeHandler(v -> {
        // Make sure the writeHandler gets unregistered
        RawWSSockJSSocket.super.close();
      });
    }

    public SockJSSocket handler(Handler<Buffer> handler) {
      ws.handler(handler);
      return this;
    }

    public SockJSSocket pause() {
      ws.pause();
      return this;
    }

    public SockJSSocket resume() {
      ws.resume();
      return this;
    }

    public SockJSSocket write(Buffer data) {
      ws.write(data);
      return this;
    }

    public SockJSSocket setWriteQueueMaxSize(int maxQueueSize) {
      ws.setWriteQueueMaxSize(maxQueueSize);
      return this;
    }

    public boolean writeQueueFull() {
      return ws.writeQueueFull();
    }

    public SockJSSocket drainHandler(Handler<Void> handler) {
      ws.drainHandler(handler);
      return this;
    }

    public SockJSSocket exceptionHandler(Handler<Throwable> handler) {
      ws.exceptionHandler(handler);
      return this;
    }

    public SockJSSocket endHandler(Handler<Void> endHandler) {
      ws.endHandler(endHandler);
      return this;
    }

    public void close() {
      super.close();
      ws.close();
    }

    @Override
    public SocketAddress remoteAddress() {
      return ws.remoteAddress();
    }

    @Override
    public SocketAddress localAddress() {
      return ws.localAddress();
    }

    @Override
    public MultiMap headers() {
      if (headers == null) {
        headers = BaseTransport.removeCookieHeaders(ws.headers());
      }
      return headers;
    }

    @Override
    public String uri() {
      return ws.uri();
    }
  }

  public RawWebSocketTransport(Vertx vertx, Router router,
                        Handler<SockJSSocket> sockHandler) {

    String wsRE = "/eventbus/websocket";

    router.get(wsRE, new Middleware() {
      @Override
      public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        ServerWebSocket ws = request.upgrade();
        SockJSSocket sock = new RawWSSockJSSocket(vertx, ws);
        sockHandler.handle(sock);
      }
    });

    router.get(wsRE, new Middleware() {
      @Override
      public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        request.response().setStatusCode(400).end("Can \"Upgrade\" only to \"WebSocket\".");
      }
    });

    router.get(wsRE, new Middleware() {
      @Override
      public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        request.response().putHeader("Allow", "GET").setStatusCode(405).end();
      }
    });
  }

}
