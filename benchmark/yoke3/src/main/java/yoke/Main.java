package yoke;

import com.jetdrone.vertx.yoke.Yoke;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;

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
      app.use((req, next) -> next.handle(null));
    }

    Buffer body = Buffer.buffer("Hello World");

    app.use((req, next) -> req.response().end(body));

    app.listen(3333);
  }
}
