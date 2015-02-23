import org.vertx.java.platform.Verticle;
import org.vertx.java.core.Handler;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.*;
import com.jetdrone.vertx.yoke.engine.*;

import java.lang.Override;

public class App extends Verticle {

  public void start() {
      // Create a new Yoke Application
      Yoke app = new Yoke(this);
      // define engines
      app.engine(new StringPlaceholderEngine("views"));
      // define middleware
      app.use(new Favicon());
      app.use(new Logger());
      app.use(new BodyParser());
      app.use(new MethodOverride());
      // Create a new Router
      Router router = new Router();
      app.use(router);
      // static file server
      app.use(new Static("public"));

      // development only
      if (System.getenv("DEV") != null) {
          app.use(new ErrorHandler(true));
      }

      // define routes
      router.get("/", new Handler<YokeRequest>() {
          @Override
          public void handle(YokeRequest request) {
              request.put("title", "My Yoke Application");
              request.response().render("views/index.shtml");
          }
      });

      // define routes
      router.get("/users", new Handler<YokeRequest>() {
          @Override
          public void handle(YokeRequest request) {
              request.response().end("respond with a resource");
          }
      });

      app.listen(8080, new Handler<Boolean>() {
          @Override
          public void handle(Boolean result) {
              getContainer().logger().info("Yoke server listening on port 8080");
          }
      });
  }
}
