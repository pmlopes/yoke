import com.jetdrone.vertx.yoke.GYoke
import com.jetdrone.vertx.yoke.annotations.GET
import com.jetdrone.vertx.yoke.middleware.Favicon
import com.jetdrone.vertx.yoke.middleware.GYokeRequest
import com.jetdrone.vertx.yoke.middleware.Router
import com.jetdrone.vertx.yoke.test.GYokeTester
import com.jetdrone.vertx.yoke.test.Response
import org.junit.Test
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.platform.Container
import org.vertx.testtools.TestVerticle

import static org.vertx.testtools.VertxAssert.*

class GTest extends TestVerticle {

    @Test
    public void testInstantiation() {
        def gYoke = new GYoke(new Vertx(vertx), new Container(container))
        gYoke.use {req ->
            req.response.end()
        }
        gYoke.use(new Favicon())

        testComplete()
    }

    class AnnotatedRouter {
        @GET('/rest')
        def getResource(GYokeRequest request) {
            request.response.end('Hello rest!');
        }
    }

    @Test
    public void testAnnotatedRouter() {
        def gYoke = new GYoke(new Vertx(vertx), new Container(container))
        gYoke.use(Router.from(new AnnotatedRouter()));

        new GYokeTester(vertx, gYoke).request("GET", "/rest") { Response resp ->
            assertEquals(200, resp.getStatusCode());
            assertEquals("Hello rest!", resp.body.toString());
            testComplete();
        }
    }
}
