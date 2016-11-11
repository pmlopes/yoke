package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class Main extends AbstractVerticle {

  @Override
  public void start() {
    final Router app = Router.router(vertx);

    // number of middleware
    int n;

    try {
      n = Integer.parseInt(System.getenv("MW"));
    } catch (RuntimeException e) {
      n = 1;
    }

    System.out.printf("  %s middleware", n);

    while (n-- != 0) {
      app.route().handler(RoutingContext::next);
    }

    Buffer body = Buffer.buffer("Hello World");

    app.route().handler(ctx -> ctx.response().end(body));

    vertx.createHttpServer().requestHandler(app::accept).listen(3333);
  }
}
