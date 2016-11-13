package xyz.jetdrone.yoke.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import org.jetbrains.annotations.NotNull;
import xyz.jetdrone.yoke.Context;
import xyz.jetdrone.yoke.impl.AbstractContext;
import xyz.jetdrone.yoke.impl.ConcatList;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Route implements Handler<Context> {

  private final String route;
  private final Pattern pattern;
  private final Set<String> paramNames;

  private final List<Handler<Context>> params = new ArrayList<>();
  private final Map<HttpMethod, List<Handler<Context>>> handlers = new IdentityHashMap<>();
  // TODO: solve method not allowed when the number of elements in the list is zero
  Route(@NotNull String mount, @NotNull Pattern pattern, Set<String> params) {
    for (HttpMethod m : HttpMethod.values()) {
      List<Handler<Context>> l = new ArrayList<>();
      handlers.put(m, l);
      if (m == HttpMethod.OPTIONS) {
        l.add(ctx -> {
          final Set<String> options = new HashSet<>();
          for (HttpMethod method : HttpMethod.values()) {
            if (handlers.get(method).size() > 1) {
              options.add(method.name());
            }
          }

          final String body = String.join(",", options);
          ctx.set("Allow", body);
          ctx.end(body);
        });
      } else {
        l.add(ctx -> {
          if (handlers.get(ctx.getRequest().method()).size() == 1) {
            final Set<String> options = new HashSet<>();
            for (HttpMethod method : HttpMethod.values()) {
              if (handlers.get(method).size() > 1) {
                options.add(method.name());
              }
            }

            final String body = String.join(",", options);
            ctx.set("Allow", body);
            ctx.fail(405);
          } else {
            ctx.next();
          }
        });
      }
    }
    this.route = mount;
    this.pattern = pattern;
    this.paramNames = params;
  }

  boolean isFor(@NotNull String route) {
    return this.route.equals(route);
  }

  boolean isFor(@NotNull Pattern regex) {
    return pattern.pattern().equals(regex.pattern());
  }

  void addHandler(@NotNull HttpMethod method, @NotNull Handler<Context> handler) {
    // we always have a last handler for options
    int last = handlers.get(method).size() - 1;
    handlers.get(method).add(last, handler);
  }

  void addParam(String param, Handler<Context> handler) {
    if (paramNames.contains(param)) {
      params.add(handler);
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
    abstractContext.setIterator(new ConcatList<>(params, handlers.get(ctx.getRequest().method())));
    ctx.next();
  }
}
