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
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.sockjs.SockJSHandlerOptions;
import com.jetdrone.vertx.yoke.sockjs.SockJSSocket;
import com.jetdrone.vertx.yoke.sockjs.Transport;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VoidHandler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.StringEscapeUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import static io.vertx.core.http.HttpHeaders.COOKIE;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BaseTransport {

  private static final Logger log = LoggerFactory.getLogger(BaseTransport.class);

  protected final Vertx vertx;
  protected final LocalMap<String, SockJSSession> sessions;
  protected SockJSHandlerOptions options;

  // TODO: the prefix needs to be configurable
  protected static final String COMMON_PATH_ELEMENT_RE = "\\/eventbus\\/[^\\/\\.]+\\/([^\\/\\.]+)\\/";

  private static final long RAND_OFFSET = 2L << 30;

  public BaseTransport(Vertx vertx, LocalMap<String, SockJSSession> sessions, SockJSHandlerOptions options) {
    this.vertx = vertx;
    this.sessions = sessions;
    this.options = options;
  }

  protected SockJSSession getSession(YokeRequest rc, long timeout, long heartbeatInterval, String sessionID,
                                     Handler<SockJSSocket> sockHandler) {
    SockJSSession session = sessions.get(sessionID);
    if (session == null) {
      session = new SockJSSession(vertx, sessions, rc, sessionID, timeout, heartbeatInterval, sockHandler);
      sessions.put(sessionID, session);
    }
    return session;
  }

  protected void sendInvalidJSON(HttpServerResponse response) {
    if (log.isTraceEnabled()) log.trace("Broken JSON");
    response.setStatusCode(500);
    response.end("Broken JSON encoding.");
  }

  protected String escapeForJavaScript(String str) {
    try {
       str = StringEscapeUtils.escapeJavaScript(str);
    } catch (Exception e) {
      log.error("Failed to escape", e);
      str = null;
    }
    return str;
  }

  protected static abstract class BaseListener implements TransportListener {
    protected final YokeRequest rc;
    protected final SockJSSession session;
    protected boolean closed;

    protected BaseListener(YokeRequest rc, SockJSSession session) {
      this.rc = rc;
      this.session = session;
    }
    protected void addCloseHandler(HttpServerResponse resp, final SockJSSession session) {
      resp.closeHandler(new VoidHandler() {
        public void handle() {
          if (log.isTraceEnabled()) log.trace("Connection closed (from client?), closing session");
          // Connection has been closed from the client or network error so
          // we remove the session
          session.shutdown();
          closed = true;
        }
      });
    }

    @Override
    public void sessionClosed() {
      session.writeClosed(this);
      close();
    }
  }

  static void setJSESSIONID(SockJSHandlerOptions options, YokeRequest request) {
    String cookies = request.getHeader("cookie");
    if (options.isInsertJSESSIONID()) {
      //Preserve existing JSESSIONID, if any
      if (cookies != null) {
        String[] parts;
        if (cookies.contains(";")) {
          parts = cookies.split(";");
        } else {
          parts = new String[] {cookies};
        }
        for (String part: parts) {
          if (part.startsWith("JSESSIONID")) {
            cookies = part + "; path=/";
            break;
          }
        }
      }
      if (cookies == null) {
        cookies = "JSESSIONID=dummy; path=/";
      }
      request.response().putHeader("Set-Cookie", cookies);
    }
  }

  public static void setCORS(@NotNull YokeRequest request) {
    String origin = request.headers().get("origin");
    if (origin == null || "null".equals(origin)) {
      origin = "*";
    }
    request.response().headers().set("Access-Control-Allow-Origin", origin);
    request.response().headers().set("Access-Control-Allow-Credentials", "true");
    String hdr = request.headers().get("Access-Control-Request-Headers");
    if (hdr != null) {
      request.response().headers().set("Access-Control-Allow-Headers", hdr);
    }
  }

  public static Middleware createInfoHandler(final SockJSHandlerOptions options) {
    return new Middleware() {
      boolean websocket = !options.getDisabledTransports().contains(Transport.WEBSOCKET.toString());
      public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        if (log.isTraceEnabled()) log.trace("In Info handler");
        request.response().putHeader("Content-Type", "application/json; charset=UTF-8");
        setNoCacheHeaders(request);
        JsonObject json = new JsonObject();
        json.put("websocket", websocket);
        json.put("cookie_needed", options.isInsertJSESSIONID());
        json.put("origins", new JsonArray().add("*:*"));
        // Java ints are signed, so we need to use a long and add the offset so
        // the result is not negative
        json.put("entropy", RAND_OFFSET + new Random().nextInt());
        setCORS(request);
        request.response().end(json.encode());
      }
    };
  }

  static void setNoCacheHeaders(YokeRequest rc) {
    rc.response().putHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
  }

  public static Middleware createCORSOptionsHandler(SockJSHandlerOptions options, String methods) {
    return new Middleware() {
      @Override
      public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        if (log.isTraceEnabled()) log.trace("In CORS options handler");
        request.response().putHeader("Cache-Control", "public,max-age=31536000");
        long oneYearSeconds = 365 * 24 * 60 * 60;
        long oneYearms = oneYearSeconds * 1000;
        String expires = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date(System.currentTimeMillis() + oneYearms));
        request.response().putHeader("Expires", expires)
            .putHeader("Access-Control-Allow-Methods", methods)
            .putHeader("Access-Control-Max-Age", String.valueOf(oneYearSeconds));
        setCORS(request);
        setJSESSIONID(options, request);
        request.response().setStatusCode(204);
        request.response().end();
      }
    };
  }

  // We remove cookie headers for security reasons. See https://github.com/sockjs/sockjs-node section on
  // Authorisation
  static MultiMap removeCookieHeaders(MultiMap headers) {
    // We don't want to remove the JSESSION cookie.
    String cookieHeader = headers.get(COOKIE);
    if (cookieHeader != null) {
      headers.remove(COOKIE);
      Set<Cookie> nettyCookies = ServerCookieDecoder.STRICT.decode(cookieHeader);
      for (Cookie cookie: nettyCookies) {
        if (cookie.name().equals("JSESSIONID")) {
          headers.add(COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
          break;
        }
      }
    }
    return headers;
  }
}
