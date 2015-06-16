package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.test.Response;
import com.jetdrone.vertx.yoke.test.YokeTester;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.vertx.groovy.core.http.HttpClientRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;
import org.vertx.testtools.TestVerticle;


import java.util.List;
import java.util.Map;

import static org.vertx.testtools.VertxAssert.*;

public class JsonStoreTest extends TestVerticle {

    protected CRUD getHappyFlowCRUD() {
        CRUD crud = new CRUD();

        crud.readHandler = new CRUD.Handler() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull JsonObject filter, @NotNull Handler<JsonObject> next) {
                JsonObject json = new JsonObject();
                json.putString("status", "ok");
                JsonArray list = new JsonArray();
                list.add(new JsonObject());
                json.putArray("value", list);

                next.handle(json);
            }
        };

        crud.createHandler = new CRUD.Handler() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull JsonObject filter, @NotNull Handler<JsonObject> next) {
                JsonObject json = new JsonObject();
                json.putString("status", "ok");
                json.putObject("value", new JsonObject());

                next.handle(json);
            }
        };

        crud.updateHandler = new CRUD.Handler() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull JsonObject filter, @NotNull Handler<JsonObject> next) {
                JsonObject json = new JsonObject();
                json.putString("status", "ok");
                json.putNumber("value", 1);

                next.handle(json);
            }
        };

        crud.deleteHandler = new CRUD.Handler() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull JsonObject filter, @NotNull Handler<JsonObject> next) {
                JsonObject json = new JsonObject();
                json.putString("status", "ok");
                json.putNumber("value", 1);

                next.handle(json);
            }
        };

        return crud;
    }

    protected CRUD getFailingCRUD() {
        CRUD crud = new CRUD();

        crud.readHandler = new CRUD.Handler() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull JsonObject filter, @NotNull Handler<JsonObject> next) {
                JsonObject json = new JsonObject();
                json.putString("status", "ok");
                JsonArray list = new JsonArray();
                json.putArray("value", list);

                next.handle(json);
            }
        };

        return crud;
    }

    protected YokeTester getTester(CRUD crud) {
        Yoke yoke = new Yoke(this);
        yoke.use(new com.jetdrone.vertx.yoke.middleware.BodyParser());

        JsonStore store = new JsonStore("/api");

        store.collection("persons", "personId", crud , null);
        yoke.use(store);

        return new YokeTester(yoke);
    }

    @Test
    public void getAllTest() {
        getTester(getHappyFlowCRUD()).request("GET", "/api/persons", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void getOneTest() {
        getTester(getHappyFlowCRUD()).request("GET", "/api/persons/1", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void getOneTestWhenNotFound() {
        getTester(getFailingCRUD()).request("GET", "/api/persons/1", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(404, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void updateTest() {
        JsonObject json = new JsonObject().putString("message", "Hello!");

        Buffer body = new Buffer(json.encode());

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/json");
        headers.add("content-length", Integer.toString(body.length()));

        getTester(getHappyFlowCRUD()).request("PUT", "/api/persons/1", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(204, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void createTest() {
        JsonObject json = new JsonObject().putString("message", "Hello!");

        Buffer body = new Buffer(json.encode());

        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add("content-type", "application/json");
        headers.add("content-length", Integer.toString(body.length()));

        getTester(getHappyFlowCRUD()).request("POST", "/api/persons", headers, body, new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(201, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void deleteTest() {
        getTester(getHappyFlowCRUD()).request("DELETE", "/api/persons/1", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(204, resp.getStatusCode());
                testComplete();
            }
        });
    }

    @Test
    public void paramsInUrlTest() {
        Yoke yoke = new Yoke(this);

        JsonStore store = new JsonStore("/api");

        CRUD crud = new CRUD();

        crud.readHandler = new CRUD.Handler() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull JsonObject filter, @NotNull Handler<JsonObject> next) {

                assertEquals(request.params().get("basketId"), "7");
                assertEquals(request.params().get("appleId"), "11");
                testComplete();
            }
        };

        store.collection("baskets/:basketId/apples", "appleId", crud , null);
        yoke.use(store);

        new YokeTester(yoke).request("GET", "/api/baskets/7/apples/11", new Handler<Response>() {
            @Override
            public void handle(Response resp) {}
        });
    }
}
