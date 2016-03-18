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
import com.jetdrone.vertx.yoke.sockjs.SockJSHandlerOptions;
import com.jetdrone.vertx.yoke.sockjs.SockJSSocket;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class WebSocketTransport extends BaseTransport {

  private static final Logger log = LoggerFactory.getLogger(WebSocketTransport.class);

  public WebSocketTransport(Vertx vertx,
                     Router router, LocalMap<String, SockJSSession> sessions,
                     SockJSHandlerOptions options,
                     Handler<SockJSSocket> sockHandler) {
    super(vertx, sessions, options);
    String wsRE = COMMON_PATH_ELEMENT_RE + "websocket";

    System.out.println(wsRE);

    router.get(Pattern.compile(wsRE), (request, next) -> {
      String connectionHeader = request.headers().get(io.vertx.core.http.HttpHeaders.CONNECTION);
      if (connectionHeader == null || !connectionHeader.toLowerCase().contains("upgrade")) {
        request.response().setStatusCode(400);
        request.response().end("Can \"Upgrade\" only to \"WebSocket\".");
      } else {
        ServerWebSocket ws = request.upgrade();
        if (log.isTraceEnabled()) log.trace("WS, handler");
        SockJSSession session = new SockJSSession(vertx, sessions, request, options.getHeartbeatInterval(), sockHandler);
        session.setInfo(ws.localAddress(), ws.remoteAddress(), ws.uri(), ws.headers());
        session.register(new WebSocketListener(ws, session));
      }
    });

    router.get(Pattern.compile(wsRE), (request, next) -> {
      if (log.isTraceEnabled()) log.trace("WS, get: " + request.uri());
      request.response().setStatusCode(400);
      request.response().end("Can \"Upgrade\" only to \"WebSocket\".");
    });

    router.all(Pattern.compile(wsRE), new Middleware() {
      @Override
      public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        if (log.isTraceEnabled()) log.trace("WS, all: " + request.uri());
        request.response().putHeader("Allow", "GET").setStatusCode(405).end();
      }
    });
  }

  private static class WebSocketListener implements TransportListener {

    final ServerWebSocket ws;
    final SockJSSession session;
    boolean closed;

    WebSocketListener(ServerWebSocket ws, SockJSSession session) {
      this.ws = ws;
      this.session = session;
      ws.handler(data -> {
        if (!session.isClosed()) {
          String msgs = data.toString();
          if (msgs.equals("")) {
            //Ignore empty frames
          } else if ((msgs.startsWith("[\"") && msgs.endsWith("\"]")) ||
                     (msgs.startsWith("\"") && msgs.endsWith("\""))) {
            session.handleMessages(msgs);
          } else {
            //Invalid JSON - we close the connection
            close();
          }
        }
      });
      ws.closeHandler(v -> {
        closed = true;
        session.shutdown();
      });
      ws.exceptionHandler(t -> {
        closed = true;
        session.shutdown();
        session.handleException(t);
      });
    }

    public void sendFrame(final String body) {
      if (log.isTraceEnabled()) log.trace("WS, sending frame");
      if (!closed) {
        ws.writeFrame(WebSocketFrame.textFrame(body, true));
      }
    }

    public void close() {
      if (!closed) {
        ws.close();
        session.shutdown();
        closed = true;
      }
    }

    public void sessionClosed() {
      session.writeClosed(this);
      closed = true;
      ws.close();
    }

  }
}
