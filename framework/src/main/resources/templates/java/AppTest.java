import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class AppTest extends TestVerticle {

    @Test
    public void testApp() {
        final Yoke yoke = new Yoke(this);
        yoke.use(new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                request.response().end("OK");
            }
        });

        final YokeTester yokeAssert = new YokeTester(yoke);

        yokeAssert.request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response response) {
                assertEquals("OK", response.body.toString());
                testComplete();
            }
        });
    }
}
