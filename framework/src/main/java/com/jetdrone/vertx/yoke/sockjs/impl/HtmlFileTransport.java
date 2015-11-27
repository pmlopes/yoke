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

import com.jetdrone.vertx.yoke.middleware.RequestProxy;
import com.jetdrone.vertx.yoke.middleware.Router;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.sockjs.SockJSHandlerOptions;
import com.jetdrone.vertx.yoke.sockjs.SockJSSocket;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;

import java.util.regex.Pattern;

import static io.vertx.core.buffer.Buffer.buffer;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HtmlFileTransport extends BaseTransport {

  private static final Logger log = LoggerFactory.getLogger(HtmlFileTransport.class);

  private static final String HTML_FILE_TEMPLATE;

  static {
    String str =
    "<!doctype html>\n" +
    "<html><head>\n" +
    "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
    "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
    "</head><body><h2>Don't panic!</h2>\n" +
    "  <script>\n" +
    "    document.domain = document.domain;\n" +
    "    var c = parent.{{ callback }};\n" +
    "    c.start();\n" +
    "    function p(d) {c.message(d);};\n" +
    "    window.onload = function() {c.stop();};\n" +
    "  </script>";

    String str2 = str.replace("{{ callback }}", "");
    StringBuilder sb = new StringBuilder(str);
    int extra = 1024 - str2.length();
    for (int i = 0; i < extra; i++) {
      sb.append(' ');
    }
    sb.append("\r\n");
    HTML_FILE_TEMPLATE = sb.toString();
  }

  public HtmlFileTransport(Vertx vertx, Router router, LocalMap<String, SockJSSession> sessions, SockJSHandlerOptions options,
                    Handler<SockJSSocket> sockHandler) {
    super(vertx, sessions, options);
    String htmlFileRE = COMMON_PATH_ELEMENT_RE + "htmlfile.*";

    router.get(Pattern.compile(htmlFileRE), (request, next) -> {
      if (log.isTraceEnabled()) log.trace("HtmlFile, get: " + request.uri());
      String callback = request.getParam("callback");
      if (callback == null) {
        callback = request.getParam("c");
        if (callback == null) {
          request.response().setStatusCode(500).end("\"callback\" parameter required\n");
          return;
        }
      }

      String sessionID = request.params().get("param0");
      SockJSSession session = getSession(request, options.getSessionTimeout(), options.getHeartbeatInterval(), sessionID, sockHandler);
      session.setInfo(request.localAddress(), request.remoteAddress(), request.uri(), request.headers());
      session.register(new HtmlFileListener(options.getMaxBytesStreaming(), request, callback, session));
    });
  }

  private class HtmlFileListener extends BaseListener {

    final int maxBytesStreaming;
    final String callback;
    boolean headersWritten;
    int bytesSent;
    boolean closed;

    HtmlFileListener(int maxBytesStreaming, YokeRequest rc, String callback, SockJSSession session) {
      super(rc, session);
      this.maxBytesStreaming = maxBytesStreaming;
      this.callback = callback;
      addCloseHandler(rc.response(), session);
    }

    public void sendFrame(String body) {
      if (log.isTraceEnabled()) log.trace("HtmlFile, sending frame");
      if (!headersWritten) {
        String htmlFile = HTML_FILE_TEMPLATE.replace("{{ callback }}", callback);
        rc.response().putHeader("Content-Type", "text/html; charset=UTF-8");
        setNoCacheHeaders(rc);
        rc.response().setChunked(true);
        setJSESSIONID(options, rc);
        rc.response().write(htmlFile);
        headersWritten = true;
      }
      body = escapeForJavaScript(body);
      StringBuilder sb = new StringBuilder();
      sb.append("<script>\np(\"");
      sb.append(body);
      sb.append("\");\n</script>\r\n");
      Buffer buff = buffer(sb.toString());
      rc.response().write(buff);
      bytesSent += buff.length();
      if (bytesSent >= maxBytesStreaming) {
        if (log.isTraceEnabled()) log.trace("More than maxBytes sent so closing connection");
        // Reset and close the connection
        close();
      }
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
