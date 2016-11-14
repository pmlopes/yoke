package xyz.jetdrone.yoke;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import org.jetbrains.annotations.NotNull;
import xyz.jetdrone.yoke.impl.AbstractContext;
import xyz.jetdrone.yoke.impl.ContextImpl;
import xyz.jetdrone.yoke.impl.MountedHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Yoke implements Handler<HttpServerRequest> {

  /**
   * default globals available to all requests
   * <p>
   * <pre>
   * {
   *   title: "Yoke",
   *   x-powered-by: true,
   *   trust-proxy: true
   * }
   * </pre>
   */
  private final Map<String, Object> globals = new HashMap<>();

  private final Vertx vertx;

  /**
   * Creates a Yoke instance.
   * <p>
   * This constructor should be called from a verticle and pass a valid Vertx instance and a Logger. This instance
   * will be shared with all registered middleware. The reason behind this is to allow middleware to use Vertx
   * features such as file system and timers.
   * <p>
   * <pre>
   * public class MyVerticle extends AbstractVerticle {
   *   public void start() {
   *     final Yoke yoke = new Yoke(getVertx());
   *     ...
   *   }
   * }
   * </pre>
   */
  public Yoke(Vertx vertx) {
    globals.put("title", "Yoke");
    globals.put("x-powered-by", true);
    globals.put("trust-proxy", true);

    this.vertx = vertx;
  }

  /**
   * Ordered list of mounted middleware in the chain
   */
  private final List<MountedHandler<Context>> handlers = new ArrayList<>();

  /**
   * Special middleware used for error handling
   */
  private Handler<Context> errorHandler = ctx -> {
    final Object error = ctx.getData().get("error");
    int errorCode;

    // if the error was set on the response use it
    if (ctx.getResponse().getStatusCode() >= 400) {
      errorCode = ctx.getResponse().getStatusCode();
    } else {
      // if it was set as the error object use it
      if (error instanceof Number) {
        errorCode = ((Number) error).intValue();
      } else {
        // default error code
        errorCode = 500;
      }
    }

    ctx.getResponse().setStatusCode(errorCode);
    ctx.getResponse().setStatusMessage(HttpResponseStatus.valueOf(errorCode).reasonPhrase());
    ctx.getResponse().end(HttpResponseStatus.valueOf(errorCode).reasonPhrase());
  };

  public Yoke use(@NotNull Handler<Context> handler) {
    return use("/", handler);
  }

  /**
   * Adds a Handler to the chain.
   * <p>
   * You might want to add a middleware that is only supposed to run on a specific route (path prefix).
   * In this case if the request path does not match the prefix the middleware is skipped automatically.
   * <p>
   * <pre>
   * yoke.use(ctx -> { ... });
   * </pre>
   *
   * @param handler The handler add to the chain
   */
  public Yoke use(@NotNull String prefix, @NotNull Handler<Context> handler) {
    handlers.add(new MountedHandler<>(prefix, handler));
    return this;
  }

  public Yoke onError(@NotNull Handler<Context> handler) {
    errorHandler = handler;
    return this;
  }

  public Map<String, Object> getGlobals() {
    return globals;
  }

  public Vertx getVertx() {
    return vertx;
  }

  @Override
  public void handle(HttpServerRequest req) {
    // the context map is shared with all middlewares
    final AbstractContext ctx = new ContextImpl(req, this);

    // add x-powered-by header is enabled
    Boolean poweredBy = (Boolean) ctx.getData().get("x-powered-by");
    if (poweredBy != null && poweredBy) {
      ctx.getResponse().putHeader("x-powered-by", "yoke");
    }

    ctx.setIterator(handlers, errorHandler);
    // start the handling
    ctx.next();
  }
}
