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

package com.jetdrone.vertx.yoke.sockjs;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.Router;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.sockjs.impl.*;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static io.vertx.core.buffer.Buffer.buffer;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SockJS extends Middleware {

  private static final Logger log = LoggerFactory.getLogger(SockJS.class);

  private Vertx vertx;
  private Router router;
  private LocalMap<String, SockJSSession> sessions;
  private SockJSHandlerOptions options;

  public SockJS(Vertx vertx, SockJSHandlerOptions options) {
    this.vertx = vertx;
    // TODO use clustered map
    this.sessions = vertx.sharedData().getLocalMap("_vertx.sockjssessions");
    this.router = new Router();
    this.options = options;
  }

  @Override
  public SockJS init(@NotNull final Yoke yoke, @NotNull final String mount) {
    super.init(yoke, mount);
    router.init(yoke, mount);

    return this;
  }

  @Override
  public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
    if (log.isTraceEnabled()) {
      log.trace("Got request in sockjs server: " + request.uri());
    }
    router.handle(request, next);
  }

  public SockJS bridge(BridgeOptions bridgeOptions) {
    return bridge(bridgeOptions, null);
  }

  public SockJS bridge(BridgeOptions bridgeOptions, Handler<BridgeEvent> bridgeEventHandler) {
    socketHandler(new EventBusBridgeImpl(vertx, bridgeOptions, bridgeEventHandler));
    return this;
  }

  public SockJS socketHandler(Handler<SockJSSocket> sockHandler) {

    // Iframe handlers
    String iframeHTML = IFRAME_TEMPLATE.replace("{{ sockjs_url }}", options.getLibraryURL());
    Middleware iframeHandler = createIFrameHandler(iframeHTML);

    // Request exactly for iframe.html
    router.get("/eventbus/iframe.html", iframeHandler);

    // Versioned
    router.get(Pattern.compile("\\/eventbus\\/iframe-[^\\/]*\\.html"), iframeHandler);

    // Chunking test
    router.post("/eventbus/chunking_test", createChunkingTestHandler());
    router.options("/eventbus/chunking_test", BaseTransport.createCORSOptionsHandler(options, "OPTIONS, POST"));

    // Info
    router.get("/eventbus/info", BaseTransport.createInfoHandler(options));
    router.options("/eventbus/info", BaseTransport.createCORSOptionsHandler(options, "OPTIONS, GET"));

    router.all("/eventbus", rc -> {
      if (log.isTraceEnabled()) log.trace("Returning welcome response");
      rc.response().putHeader("Content-Type", "text/plain; charset=UTF-8").end("Welcome to SockJS!\n");
    });

    // Transports

    Set<String> enabledTransports = new HashSet<>();
    enabledTransports.add(Transport.EVENT_SOURCE.toString());
    enabledTransports.add(Transport.HTML_FILE.toString());
    enabledTransports.add(Transport.JSON_P.toString());
    enabledTransports.add(Transport.WEBSOCKET.toString());
    enabledTransports.add(Transport.XHR.toString());
    Set<String> disabledTransports = options.getDisabledTransports();
    if (disabledTransports == null) {
      disabledTransports = new HashSet<>();
    }
    enabledTransports.removeAll(disabledTransports);

    if (enabledTransports.contains(Transport.XHR.toString())) {
      new XhrTransport(vertx, router, sessions, options, sockHandler);
    }
    if (enabledTransports.contains(Transport.EVENT_SOURCE.toString())) {
      new EventSourceTransport(vertx, router, sessions, options, sockHandler);
    }
    if (enabledTransports.contains(Transport.HTML_FILE.toString())) {
      new HtmlFileTransport(vertx, router, sessions, options, sockHandler);
    }
    if (enabledTransports.contains(Transport.JSON_P.toString())) {
      new JsonPTransport(vertx, router, sessions, options, sockHandler);
    }
    if (enabledTransports.contains(Transport.WEBSOCKET.toString())) {
      new WebSocketTransport(vertx, router, sessions, options, sockHandler);
      new RawWebSocketTransport(vertx, router, sockHandler);
    }

    return this;
  }

  private Middleware createChunkingTestHandler() {
    return new Middleware() {

      class TimeoutInfo {
        long timeout;
        Buffer buff;

        TimeoutInfo(long timeout, Buffer buff) {
          this.timeout = timeout;
          this.buff = buff;
        }
      }

      private void setTimeout(List<TimeoutInfo> timeouts, long delay, Buffer buff) {
        timeouts.add(new TimeoutInfo(delay, buff));
      }

      private void runTimeouts(List<TimeoutInfo> timeouts, HttpServerResponse response) {
        Iterator<TimeoutInfo> iter = timeouts.iterator();
        nextTimeout(timeouts, iter, response);
      }

      private void nextTimeout(List<TimeoutInfo> timeouts, Iterator<TimeoutInfo> iter, HttpServerResponse response) {
        if (iter.hasNext()) {
          TimeoutInfo timeout = iter.next();
          vertx.setTimer(timeout.timeout, id -> {
            response.write(timeout.buff);
            nextTimeout(timeouts, iter, response);
          });
        } else {
          timeouts.clear();
        }
      }

      @Override
      public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        request.response().headers().set("Content-Type", "application/javascript; charset=UTF-8");

        BaseTransport.setCORS(request);
        request.response().setChunked(true);

        Buffer h = buffer(2);
        h.appendString("h\n");

        Buffer hs = buffer(2050);
        for (int i = 0; i < 2048; i++) {
          hs.appendByte((byte) ' ');
        }
        hs.appendString("h\n");

        List<TimeoutInfo> timeouts = new ArrayList<>();

        setTimeout(timeouts, 0, h);
        setTimeout(timeouts, 1, hs);
        setTimeout(timeouts, 5, h);
        setTimeout(timeouts, 25, h);
        setTimeout(timeouts, 125, h);
        setTimeout(timeouts, 625, h);
        setTimeout(timeouts, 3125, h);

        runTimeouts(timeouts, request.response());
      }
    };
  }

  private Middleware createIFrameHandler(String iframeHTML) {
    String etag = getMD5String(iframeHTML);
    return new Middleware() {
      @Override
      public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        try {
          if (log.isTraceEnabled()) log.trace("In Iframe handler");
          if (etag != null && etag.equals(request.getHeader("if-none-match"))) {
            request.response().setStatusCode(304);
            request.response().end();
          } else {
            long oneYear = 365 * 24 * 60 * 60 * 1000L;
            String expires = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date(System.currentTimeMillis() + oneYear));
            request.response().putHeader("Content-Type", "text/html; charset=UTF-8")
                .putHeader("Cache-Control", "public,max-age=31536000")
                .putHeader("Expires", expires).putHeader("ETag", etag).end(iframeHTML);
          }
        } catch (Exception e) {
          log.error("Failed to server iframe", e);
        }
      }
    };
  }

  private static String getMD5String(String str) {
    try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(str.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
          sb.append(Integer.toHexString(b + 127));
        }
        return sb.toString();
    }
    catch (Exception e) {
        log.error("Failed to generate MD5 for iframe, If-None-Match headers will be ignored");
        return null;
    }
  }

  private static final String IFRAME_TEMPLATE =
      "<!DOCTYPE html>\n" +
      "<html>\n" +
      "<head>\n" +
      "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
      "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
      "  <script>\n" +
      "    document.domain = document.domain;\n" +
      "    _sockjs_onload = function(){SockJS.bootstrap_iframe();};\n" +
      "  </script>\n" +
      "  <script src=\"{{ sockjs_url }}\"></script>\n" +
      "</head>\n" +
      "<body>\n" +
      "  <h2>Don't panic!</h2>\n" +
      "  <p>This is a SockJS hidden iframe. It's used for cross domain magic.</p>\n" +
      "</body>\n" +
      "</html>";
}

