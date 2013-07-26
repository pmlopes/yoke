import com.jetdrone.vertx.yoke.GYoke
import com.jetdrone.vertx.yoke.middleware.Favicon
import org.junit.Test
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.platform.Container
import org.vertx.testtools.TestVerticle

import static org.vertx.testtools.VertxAssert.*

class GTest extends TestVerticle {

    @Test
    public void testInstantiation() {
        def gYoke = new GYoke(new Vertx(vertx), container.logger())
        gYoke.use {req ->
            req.response.end()
        }
        gYoke.use(new Favicon())

        testComplete()
    }
}
