package xyz.jetdrone.yoke.impl;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.jetbrains.annotations.NotNull;
import xyz.jetdrone.yoke.Context;

import java.util.List;

public abstract class AbstractContext implements Context {

  private volatile HandlerIterator iterator;

  private final HttpServerRequest req;
  private final HttpServerResponse res;

  AbstractContext(@NotNull HttpServerRequest req) {
    this.req = req;
    this.res = req.response();
  }

  @Override
  public HttpServerRequest getRequest() {
    return req;
  }

  @Override
  public HttpServerResponse getResponse() {
    return res;
  }

  public void setIterator(List<? extends Handler<Context>> handlers) {
    iterator = new HandlerIterator(handlers, iterator);
  }

  public void setIterator(List<? extends Handler<Context>> handlers, Handler<Context> errHandler) {
    iterator = new HandlerIterator(handlers, errHandler, this);
  }

  void setIterator(HandlerIterator iterator) {
    this.iterator = iterator;
  }

  @Override
  public void next() {
    iterator.handle(null);
  }

  @Override
  public void fail(int status, String message) {
    res
      .setStatusCode(status)
      .setStatusMessage(message);

    iterator.handle(new RuntimeException(message));
  }

  @Override
  public void fail(int status, String message, Throwable cause) {
    res
      .setStatusCode(status)
      .setStatusMessage(message);

    iterator.handle(cause);
  }
}
