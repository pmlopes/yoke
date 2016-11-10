package xyz.jetdrone.yoke.impl;

import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;

public class MountedHandler<T> implements Handler<T> {

  public final String prefix;
  public final Handler<T> handler;

  public boolean enabled = true;

  public MountedHandler(@NotNull String prefix, @NotNull Handler<T> handler) {
    this.prefix = prefix;
    this.handler = handler;
  }

  @Override
  public void handle(T event) {
    handler.handle(event);
  }
}
