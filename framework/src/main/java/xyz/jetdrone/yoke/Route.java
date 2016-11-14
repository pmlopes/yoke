package xyz.jetdrone.yoke;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Route {

  private final Map<HttpMethod, List<Handler<Context>>> handlers = new IdentityHashMap<>();

  public boolean handlesMethod(HttpMethod method) {
    if (handlers.get(method) != null) {
      return true;
    }

    return method == HttpMethod.HEAD && handlers.get(HttpMethod.GET) != null;
  }

  public Set<String> getOptions() {
    Set<String> methods = new HashSet<>();

    for (HttpMethod method : handlers.keySet()) {
      methods.add(method.name());
    }

    // append automatic head
    if (this.handlers.get(HttpMethod.GET) != null && handlers.get(HttpMethod.HEAD) == null) {
      methods.add(HttpMethod.HEAD.name());
    }

    return methods;
  }

  public List<Handler<Context>> getHandlers(HttpMethod method) {
    if (method == HttpMethod.HEAD && handlers.get(HttpMethod.HEAD) == null) {
      return handlers.get(HttpMethod.GET);
    }

    return handlers.get(method);
  }

  void addHandler(@NotNull HttpMethod method, @NotNull Handler<Context> handler) {
    if (!handlers.containsKey(method)) {
      handlers.put(method, new ArrayList<>());
    }
    handlers.get(method).add(handler);
  }
}
