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

import com.jetdrone.vertx.yoke.middleware.Router;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.sockjs.SockJSHandlerOptions;
import com.jetdrone.vertx.yoke.sockjs.SockJSSocket;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class JsonPTransport extends BaseTransport {

  private static final Logger log = LoggerFactory.getLogger(JsonPTransport.class);

  public JsonPTransport(Vertx vertx, Router router, LocalMap<String, SockJSSession> sessions, SockJSHandlerOptions options,
                 Handler<SockJSSocket> sockHandler) {
    super(vertx, sessions, options);

    String jsonpRE = COMMON_PATH_ELEMENT_RE + "jsonp";

    router.get(Pattern.compile(jsonpRE), (request, next) -> {
      if (log.isTraceEnabled()) log.trace("JsonP, get: " + request.uri());
      String callback = request.getParam("callback");
      if (callback == null) {
        callback = request.getParam("c");
        if (callback == null) {
          request.response().setStatusCode(500);
          request.response().end("\"callback\" parameter required\n");
          return;
        }
      }

      String sessionID = request.params().get("param0");
      SockJSSession session = getSession(request, options.getSessionTimeout(), options.getHeartbeatInterval(), sessionID, sockHandler);
      session.setInfo(request.localAddress(), request.remoteAddress(), request.uri(), request.headers());
      session.register(new JsonPListener(request, session, callback));
    });

    String jsonpSendRE = COMMON_PATH_ELEMENT_RE + "jsonp_send";

    router.post(Pattern.compile(jsonpSendRE), (request, next) -> {
      if (log.isTraceEnabled()) log.trace("JsonP, post: " + request.uri());
      String sessionID = request.getParam("param0");
      final SockJSSession session = sessions.get(sessionID);
      if (session != null && !session.isClosed()) {
        handleSend(request, session);
      } else {
        request.response().setStatusCode(404);
        setJSESSIONID(options, request);
        request.response().end();
      }
    });
  }

  private void handleSend(YokeRequest rc, SockJSSession session) {
    rc.bodyHandler(buff -> {
      String body = buff.toString();

      boolean urlEncoded;
      String ct = rc.getHeader("content-type");
      if ("application/x-www-form-urlencoded".equalsIgnoreCase(ct)) {
        urlEncoded = true;
      } else if ("text/plain".equalsIgnoreCase(ct)) {
        urlEncoded = false;
      } else {
        rc.response().setStatusCode(500);
        rc.response().end("Invalid Content-Type");
        return;
      }

      if (body.equals("") || urlEncoded && (!body.startsWith("d=") || body.length() <= 2)) {
        rc.response().setStatusCode(500).end("Payload expected.");
        return;
      }

      if (urlEncoded) {
        try {
          body = URLDecoder.decode(body, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          throw new IllegalStateException("No UTF-8!");
        }
        body = body.substring(2);
      }

      if (!session.handleMessages(body)) {
        sendInvalidJSON(rc.response());
      } else {
        setJSESSIONID(options, rc);
        rc.response().putHeader("Content-Type", "text/plain; charset=UTF-8");
        setNoCacheHeaders(rc);
        rc.response().end("ok");
        if (log.isTraceEnabled()) log.trace("send handled ok");
      }
    });
  }

  private class JsonPListener extends BaseListener {

    final String callback;
    boolean headersWritten;
    boolean closed;

    JsonPListener(YokeRequest rc, SockJSSession session, String callback) {
      super(rc, session);
      this.callback = callback;
      addCloseHandler(rc.response(), session);
    }


    public void sendFrame(String body) {

      if (log.isTraceEnabled()) log.trace("JsonP, sending frame");

      if (!headersWritten) {
        rc.response().setChunked(true).putHeader("Content-Type", "application/javascript; charset=UTF-8");
        setNoCacheHeaders(rc);
        setJSESSIONID(options, rc);
        headersWritten = true;
      }

      body = escapeForJavaScript(body);

      StringBuilder sb = new StringBuilder();
      sb.append(callback).append("(\"");
      sb.append(body);
      sb.append("\");\r\n");

      //End the response and close the HTTP connection

      rc.response().write(sb.toString());
      close();
    }

    public void close() {
      if (!closed) {
        try {
          session.resetListener();
          rc.response().end();
          rc.response().close();
          closed = true;
        } catch (IllegalStateException e) {
          // Underlying connection might already be closed - that's fine
        }
      }
    }
  }
}
