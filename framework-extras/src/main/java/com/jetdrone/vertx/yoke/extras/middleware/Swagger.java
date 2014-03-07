package com.jetdrone.vertx.yoke.extras.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Swagger extends Middleware {

    static class Resource {

        final String path;
        final JsonObject models;
        final JsonObject apis = new JsonObject();

        Resource(String path, JsonObject models) {
            this.path = path;
            this.models = models == null ? new JsonObject() : models;
        }

        private JsonObject getApi(String path) {
            JsonObject api = apis.getObject(path);
            if (api == null) {
                api = new JsonObject()
                        .putString("path", path)
                        .putString("description", "")
                        .putArray("operations", new JsonArray());

                apis.putObject(path, api);
            }
            return api;
        }

        void get(String path, JsonObject operation) {
            get(path, "", operation);

        }
        void get(String path, String summary, JsonObject operation) {
            verb("GET", path, summary, operation);
        }
        void post(String path, JsonObject operation) {
            post(path, "", operation);
        }
        void post(String path, String summary, JsonObject operation) {
            verb("POST", path, summary, operation);
        }
        void put(String path, JsonObject operation) {
            put(path, "", operation);
        }
        void put(String path, String summary, JsonObject operation) {
            verb("PUT", path, summary, operation);
        }
        void delete(String path, JsonObject operation) {
            delete(path, "", operation);
        }
        void delete(String path, String summary, JsonObject operation) {
            verb("DELETE", path, summary, operation);
        }

        private void verb(String verb, String path, String summary, JsonObject operation) {
            operation.putString("summary", summary);
            operation.putString("httpMethod", verb);

            JsonObject api = getApi(path);
            api.getArray("operations").addObject(operation);
        }
    }

    private final List<Resource> resources = new ArrayList<>();

    private final String apiVersion;
    private final String basePath;

    public Swagger() {
        this("0.1", null);
    }

    public Swagger(String basePath) {
        this("0.1", basePath);
    }

    public Swagger(String apiVersion, String basePath) {
        this.apiVersion = apiVersion;
        this.basePath = basePath;
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        if ("GET".equals(request.method())) {
            JsonObject result = new JsonObject()
                    .putString("swaggerVersion", "1.0")
                    .putString("apiVersion", apiVersion)
                    .putString("basePath", basePath == null ? "http://" + request.getHeader("host", "localhost") : basePath);

            JsonArray apis = new JsonArray();
            result.putArray("apis", apis);

            for (Resource r : resources) {
                apis.addObject(new JsonObject()
                        .putString("path", r.path)
                        .putString("description", ""));
            }

            request.response().end(result);
        } else {
            next.handle(405);
        }
    }

    public void createResource(Yoke yoke, final String path) {
        final Swagger.Resource resource = new Swagger.Resource(path, null);
        resources.add(resource);

        yoke.use(path, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                if ("GET".equals(request.method())) {
                    // GET
                    JsonObject result = new JsonObject()
                            .putString("swaggerVersion", "1.0")
                            .putString("apiVersion", apiVersion)
                            .putString("basePath", basePath == null ? "http://" + request.getHeader("host", "localhost") : basePath);

                    result.putString("resourcePath", path);
                    JsonArray apis = new JsonArray();
                    result.putArray("apis", apis);

//                    result.apis = Object.keys(resource.apis).map(function(k) { return resource.apis[k]; });
                    result.putObject("models", resource.models);
                    request.response().end(result);
                }
            }
        });
    }
}
