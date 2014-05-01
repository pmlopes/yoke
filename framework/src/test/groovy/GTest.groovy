import com.jetdrone.vertx.yoke.GYoke
import com.jetdrone.vertx.yoke.annotations.GET
import com.jetdrone.vertx.yoke.annotations.Produces
import com.jetdrone.vertx.yoke.annotations.RegExParam
import com.jetdrone.vertx.yoke.middleware.Favicon
import com.jetdrone.vertx.yoke.middleware.GRouter
import com.jetdrone.vertx.yoke.middleware.GYokeRequest
import com.jetdrone.vertx.yoke.test.GYokeTester
import com.jetdrone.vertx.yoke.test.Response
import org.junit.Test
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.platform.Container
import org.vertx.java.core.Handler
import org.vertx.testtools.TestVerticle

import java.util.regex.Pattern

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
        def getResource(GYokeRequest request, def next) {
            request.response.end('Hello rest!');
        }
    }

    @Test
    public void testAnnotatedRouter() {
        def gYoke = new GYoke(new Vertx(vertx), new Container(container))
        gYoke.use(GRouter.from(new AnnotatedRouter()));

        new GYokeTester(vertx, gYoke).request("GET", "/rest") { Response resp ->
            assertEquals(200, resp.getStatusCode());
            assertEquals("Hello rest!", resp.body.toString());
            testComplete();
        }
    }

    @Produces('application/json')
    class Ann2 {
        @RegExParam('username')
        public final Pattern username = ~/^[a-zA-Z0-9]+$/

        @RegExParam('username2')
        public final Pattern username2 = ~/^[a-zA-Z0-9]+$/

        @GET('/users/:username')
        def getUser(GYokeRequest request, Handler next) {
            def userName = request.params['username']

            println userName
            request.response.end([:])
        }
    }

    @Test
    public void testAnnotatedRouter2() {
        def gYoke = new GYoke(new Vertx(vertx), new Container(container))
        gYoke.use(GRouter.from(new Ann2()));

        new GYokeTester(vertx, gYoke).request("GET", "/users/Paulo") { Response resp ->
            assertEquals(200, resp.getStatusCode());
            testComplete();
        }
    }
}
