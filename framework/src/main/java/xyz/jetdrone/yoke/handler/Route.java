package xyz.jetdrone.yoke.handler;

import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;
import xyz.jetdrone.yoke.Context;
import xyz.jetdrone.yoke.impl.AbstractContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Route implements Handler<Context> {

  private final String route;
  private final Pattern pattern;
  private final Set<String> paramNames;

  private final List<Handler<Context>> handlers = new ArrayList<>();

  private int params = 0;

  Route(@NotNull String mount, @NotNull Pattern pattern, Set<String> params) {
    this.route = mount;
    this.pattern = pattern;
    this.paramNames = params;
  }

  Route(@NotNull String mount, @NotNull Pattern pattern) {
    this.route = mount;
    this.pattern = pattern;
    this.paramNames = Collections.emptySet();
  }

  boolean isFor(@NotNull String route) {
    return this.route.equals(route);
  }

  boolean isFor(@NotNull Pattern regex) {
    return pattern.pattern().equals(regex.pattern());
  }

  void addHandler(@NotNull Handler<Context> handler) {
    handlers.add(handler);
  }

  void addParam(String param, Handler<Context> handler) {
    if (paramNames.contains(param)) {
      handlers.add(params++, handler);
    }
  }

  @Override
  public void handle(Context ctx) {
    final AbstractContext abstractContext = (AbstractContext) ctx;
    String prefix = ctx.getPrefix();
    int skip = prefix.length();

    if (prefix.endsWith("/")) {
      skip--;
    }
    // skip prefix for matching
    final Matcher m = pattern.matcher(skip != 0 ? ctx.getNormalizedPath().substring(skip) : ctx.getNormalizedPath());

    if (!m.matches()) {
      ctx.next();
      return;
    }

    // first need to process params
    if (paramNames != null) {
      // there are named params
      for (String param : paramNames) {
        ctx.getRequest().params().set(param, m.group(param));
      }
    } else {
      // Un-named params
      for (int i = 0; i < m.groupCount(); i++) {
        ctx.getRequest().params().set("param" + i, m.group(i + 1));
      }
    }

    // normal flow handling
    abstractContext.setIterator(handlers);
    ctx.next();
  }
}
