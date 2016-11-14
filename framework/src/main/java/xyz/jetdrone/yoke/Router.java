/*
 * Copyright 2011-2014 the original author or authors.
 */
package xyz.jetdrone.yoke;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import org.jetbrains.annotations.NotNull;
import xyz.jetdrone.yoke.impl.tree.Node;
import xyz.jetdrone.yoke.impl.tree.Tree;
import xyz.jetdrone.yoke.impl.AbstractContext;
import xyz.jetdrone.yoke.impl.util.ConcatList;

import java.util.*;
import java.util.regex.Pattern;

/**
 * # Router
 * <p>
 * Route a request by path. All *HTTP* verbs are available:
 * <p>
 * * `GET`
 * * `PUT`
 * * `POST`
 * * `DELETE`
 * * `OPTIONS`
 * * `HEAD`
 * * `TRACE`
 * * `CONNECT`
 * * `PATCH`
 *
 * Create a new Router.
 *
 * <pre>
 * new Router()
 *   .get("/hello", ctx -> {
 *     ctx.end("Hello World!");
 *   });
 * </pre>
 *
 * Path parameters can be declared using the notation: <pre>:variable_name</pre>. This notation will match per path
 * segment. In order to catch an argument as a wildcard the desired way is to use: <pre>*variable_name</pre>. The
 * wildcard mode will include all path slashes from the start.
 *
 * In order to prevent complex setup and parsing routers are not allowed to be chained.
 */
public final class Router implements Handler<Context> {

  private final Tree<Route> routes = new Tree<>();
  private final Map<String, Handler<Context>> paramHandlers = new HashMap<>();

  @Override
  public void handle(@NotNull final Context ctx) {
    final Map<String, String> params = new LinkedHashMap<>();
    String prefix = ctx.getPrefix();
    int skip = prefix.length();
    if (prefix.endsWith("/")) {
      skip--;
    }

    final Route route = routes.find(skip != 0 ? ctx.getNormalizedPath().substring(skip) : ctx.getNormalizedPath(), params);

    if (route != null) {
      final HttpMethod method = ctx.getRequest().method();

      boolean hasMethod = route.handlesMethod(method);

      // build up automatic options response
      if (!hasMethod && method == HttpMethod.OPTIONS) {
        final String body = String.join(",", route.getOptions());
        ctx.set("Allow", body);
        ctx.end(body);
        return;
      }

      // don't even bother matching route
      if (!hasMethod && method != HttpMethod.HEAD) {
        ctx.next();
        return;
      }

      // for each path param add find from handlers + the handlers from the route,  we need to reroute get to head if no head
      List<Handler<Context>> l = null;

      for (Map.Entry<String, String> kv : params.entrySet()) {
        // merge params
        ctx.getRequest().params().add(kv.getKey(), kv.getValue());
        // add the param handler if existing
        Handler<Context> h = paramHandlers.get(kv.getKey());
        if (h != null) {
          if (l == null) {
            l = new ArrayList<>();
          }
          l.add(h);
        }
      }

      // normal flow handling
      if (l != null) {
        ((AbstractContext) ctx).setIterator(new ConcatList<>(l, route.getHandlers(method)));
      } else {
        ((AbstractContext) ctx).setIterator(route.getHandlers(method));
      }
      // start
      ctx.next();
    } else {
      // no routes found, go to next handler
      ctx.next();
    }
  }

  /**
   * Specify a handlers that will be called for a matching HTTP GET
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router get(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addToRoute(HttpMethod.GET, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP PUT
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router put(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addToRoute(HttpMethod.PUT, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP POST
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router post(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addToRoute(HttpMethod.POST, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP DELETE
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router delete(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addToRoute(HttpMethod.DELETE, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP OPTIONS
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router options(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addToRoute(HttpMethod.OPTIONS, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP HEAD
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router head(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addToRoute(HttpMethod.HEAD, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP TRACE
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router trace(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addToRoute(HttpMethod.TRACE, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP CONNECT
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router connect(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addToRoute(HttpMethod.CONNECT, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for a matching HTTP PATCH
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router patch(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    addToRoute(HttpMethod.PATCH, pattern, handler);
    return this;
  }

  /**
   * Specify a handlers that will be called for all HTTP methods
   *
   * @param pattern The simple pattern
   * @param handler The handlers to call
   */
  public Router all(@NotNull final String pattern, @NotNull final Handler<Context> handler) {
    get(pattern, handler);
    put(pattern, handler);
    post(pattern, handler);
    delete(pattern, handler);
    options(pattern, handler);
    head(pattern, handler);
    trace(pattern, handler);
    connect(pattern, handler);
    patch(pattern, handler);
    return this;
  }

  public Router param(@NotNull final String paramName, @NotNull final Handler<Context> handler) {
    if (paramHandlers.containsKey(paramName)) {
      throw new IllegalStateException("There is already a parameter handler for '" + paramName + "'");
    }

    paramHandlers.put(paramName, handler);
    return this;
  }

  public Router param(@NotNull final String paramName, @NotNull final Pattern regex) {
    return param(paramName, (@NotNull final Context ctx) -> {
      if (!regex.matcher(ctx.getRequest().getParam(paramName)).matches()) {
        // Bad Request
        ctx.fail(400);
        return;
      }

      ctx.next();
    });
  }

  private void addToRoute(HttpMethod verb, String input, Handler<Context> handler) {
    if (handler instanceof Router) {
      throw new UnsupportedOperationException("Cannot mount router on router");
    }

    // verify if the binding already exists, if yes add to it
    Route route = walk(routes, input);
    // this is a new route
    if (route == null) {
      route = new Route();
      routes.add(input, route);
    }

    route.addHandler(verb, handler);
  }

  private static Route walk(Tree<Route> tree, String key) {
    return walkNode(tree.root(), key);
  }

  private static Route walkNode(Node<Route> node, String key) {
    if (key.equals(node.key())) {
      return node.value();
    } else {
      for (Node<Route> child : node.children()) {
        Route r = walkNode(child, key);
        if (r != null) {
          return r;
        }
      }
    }

    return null;
  }
}
