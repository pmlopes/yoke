package xyz.jetdrone.yoke.impl;


import io.vertx.core.Handler;
import xyz.jetdrone.yoke.Context;

import java.util.List;

final class HandlerIterator implements Handler<Throwable> {

  private static final int START = 0;
  private static final int RUNNING = 1;
  private static final int AWAIT = 2;
  private static final int COMPLETE = 3;

  private final List<? extends Handler<Context>> handlers;
  private final Handler<Context> errHandler;
  private final Handler<Context> endHandler;

  private final Context ctx;

  private int idx = -1;
  private volatile int state = START;

  HandlerIterator(List<? extends Handler<Context>> handlers, Handler<Context> errHandler, Context ctx) {
    this.handlers = handlers;
    this.errHandler = errHandler;
    this.ctx = ctx;

    this.endHandler = ctx1 -> {
      ctx1.setStatus(404);
      errHandler.handle(ctx1);
    };
  }

  HandlerIterator(List<? extends Handler<Context>> handlers, HandlerIterator parent) {
    this.handlers = handlers;
    this.errHandler = parent.errHandler;
    this.endHandler = ctx -> {
      ((AbstractContext) ctx).setIterator(parent);
      ctx.next();
    };
    this.ctx = parent.ctx;
  }

  @Override
  public void handle(Throwable err) {
    if (err != null) {
      ctx.getData().put("error", err);
      errHandler.handle(ctx);
      return;
    }

    if (state == RUNNING) {
      // this was a recursive call (blocking code)
      state = COMPLETE;
      return;
    }
    while (++idx < handlers.size()) {
      final Handler<Context> handler = handlers.get(idx);
      if (handler instanceof MountedHandler) {
        // match prefix
        final String prefix = ((MountedHandler) handler).prefix;
        if (ctx.getRequest().path().startsWith(prefix)) {
          ((ContextImpl) ctx).setPrefix(prefix);
        } else {
          // the handler was not prefixed on this uri
          // skip to the next entry
          continue;
        }
      }
      state = RUNNING;
      handler.handle(ctx);
      if (state == COMPLETE) {
        continue;
      }
      state = AWAIT;
      return;
    }

    // no more handlers
    endHandler.handle(ctx);
  }
}
