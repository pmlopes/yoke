import com.jetdrone.vertx.yoke.Yoke;
import org.vertx.java.platform.Verticle;

public class App extends Verticle {

  public void start() {
      new Yoke(this);
  }
}
