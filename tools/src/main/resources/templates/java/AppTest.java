import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class AppTest extends TestVerticle {

    @Test
    public void testApp() {
        final YokeTester yoke = new YokeTester(this);
        yoke.use(new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                assertNotNull(this.vertx);
                request.response().end("OK");
            }
        });

        yoke.request("GET", "/", new Handler<Response>() {
            @Override
            public void handle(Response response) {
                assertEquals("OK", response.body.toString());
                testComplete();
            }
        });
    }
}
