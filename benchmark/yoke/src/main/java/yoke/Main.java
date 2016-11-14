package yoke;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import xyz.jetdrone.yoke.Context;
import xyz.jetdrone.yoke.Yoke;

public class Main extends AbstractVerticle {

  @Override
  public void start() {
    final Yoke app = new Yoke(vertx);

    // number of middleware
    int n;

    try {
      n = Integer.parseInt(System.getenv("MW"));
    } catch (RuntimeException e) {
      n = 1;
    }

    System.out.printf("  %s middleware", n);


    while (n-- != 0) {
      app.use(Context::next);
    }

    Buffer body = Buffer.buffer("Hello World");

    app.use(ctx -> ctx.binary(body));

    vertx.createHttpServer().requestHandler(app).listen(3333);
  }
}
