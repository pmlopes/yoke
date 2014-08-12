import com.jetdrone.vertx.yoke.GYoke
import com.jetdrone.vertx.yoke.test.GYokeTester
import org.junit.Test
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.platform.Container
import org.vertx.testtools.TestVerticle

import static org.vertx.testtools.VertxAssert.*

public class AppTest extends TestVerticle {

    @Test
    public void testApp() {
        final GYoke yoke = new GYoke(new Vertx(vertx), new Container(container))
        yoke.use() { request, next ->
            request.response.end("OK")
        }

        final GYokeTester yokeAssert = new GYokeTester(yoke)

        yokeAssert.request("GET", "/") { response ->
            assertEquals("OK", response.body.toString())
            testComplete()
        }
    }
}
