package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.rest.Store;
import org.junit.Test;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

public class RestTest extends TestVerticle {

    private Store dummyStore = new Store() {
        @Override
        public void create(String entity, JsonObject object, AsyncResultHandler<String> response) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void read(String entity, String id, AsyncResultHandler<JsonObject> response) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void update(String entity, String id, JsonObject object, AsyncResultHandler<Number> response) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void delete(String entity, String id, AsyncResultHandler<Number> response) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void query(String entity, JsonObject query, Number start, Number end, JsonObject sort, AsyncResultHandler<JsonArray> response) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void count(String entity, JsonObject query, AsyncResultHandler<Number> response) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    @Test
    public void restTest() {

        Yoke yoke = new Yoke(this);

        JsonRestRouter router = new JsonRestRouter(dummyStore)
                .rest("/persons", "persons");

        yoke.use(router);

        testComplete();
    }
}
