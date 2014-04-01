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

    public static class Resource {

        final String path;
        final String description;

        JsonArray produces;

        JsonObject models = new JsonObject();
        JsonObject apis = new JsonObject();

        Resource(String path, String description) {
            this.path = path;
            this.description = description;
        }

        private JsonObject getApi(String path) {
            JsonObject api = apis.getObject(path);
            if (api == null) {
                api = new JsonObject()
                        .putString("path", path)
                        .putArray("operations", new JsonArray());

                apis.putObject(path, api);
            }
            return api;
        }

        public Resource produces(String... mimes) {
            produces = new JsonArray();
            for (String mime : mimes) {
                produces.addString(mime);
            }

            return this;
        }

        public void get(String path, JsonObject operation) {
            get(path, "", operation);
        }
        public void get(String path, String summary, JsonObject operation) {
            verb("GET", path, summary, operation);
        }
        public void post(String path, JsonObject operation) {
            post(path, "", operation);
        }
        public void post(String path, String summary, JsonObject operation) {
            verb("POST", path, summary, operation);
        }
        public void put(String path, JsonObject operation) {
            put(path, "", operation);
        }
        public void put(String path, String summary, JsonObject operation) {
            verb("PUT", path, summary, operation);
        }
        public void delete(String path, JsonObject operation) {
            delete(path, "", operation);
        }
        public void delete(String path, String summary, JsonObject operation) {
            verb("DELETE", path, summary, operation);
        }

        // TODO: missing verbs

        public Resource addModel(String name, JsonObject model) {
            models.putObject(name, model);
            return this;
        }

        private void verb(String verb, String path, String summary, JsonObject operation) {
            operation.putString("method", verb);
            operation.putString("summary", summary);

            JsonObject api = getApi(path);
            api.getArray("operations").addObject(operation);
        }
    }

    private final List<Resource> resources = new ArrayList<>();

    private final String apiVersion;
    private JsonObject info;
    private JsonObject authorizations;

    public Swagger(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    // TODO: validation
    public Swagger setInfo(JsonObject info) {
        this.info = info;
        return this;
    }

    // TODO: validation
    public Swagger setAuthorizations(JsonObject authorizations) {
        this.authorizations = authorizations;
        return this;
    }

    private String getBasePath() {
        if ("/".equals(mount)) {
            return "/api-docs";
        } else {
            return mount + "/api-docs";
        }
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        String path = getBasePath();

        if (path.equals(request.normalizedPath())) {
            if ("GET".equals(request.method())) {
                JsonObject result = new JsonObject()
                        .putString("apiVersion", apiVersion)
                        .putString("swaggerVersion", "1.2");

                JsonArray apis = new JsonArray();
                result.putArray("apis", apis);

                for (Resource r : resources) {
                    apis.addObject(new JsonObject()
                            .putString("path", r.path)
                            .putString("description", r.description));
                }

                if (authorizations != null) {
                    result.putObject("authorizations", authorizations);
                }

                result.putObject("info", info);

                request.response().end(result);
            } else {
                next.handle(405);
            }
        } else {
            next.handle(null);
        }
    }

    public Swagger.Resource createResource(Yoke yoke, final String path) {
        return createResource(yoke, path, "");
    }

    public Swagger.Resource createResource(Yoke yoke, final String path, final String description) {

        final String normalizedPath;

        if (path.charAt(0) != '/') {
            normalizedPath = "/" + path;
        } else {
            normalizedPath = path;
        }

        final Swagger.Resource resource = new Swagger.Resource(normalizedPath, description);
        resources.add(resource);

        yoke.use(getBasePath() + normalizedPath, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                if ("GET".equals(request.method())) {
                    if (!request.normalizedPath().equals(getBasePath() + normalizedPath)) {
                        next.handle(null);
                        return;
                    }

                    JsonObject result = new JsonObject()
                            .putString("apiVersion", apiVersion)
                            .putString("swaggerVersion", "1.2")
                            .putString("basePath", path)
                            .putString("resourcePath", path);

                    if (resource.produces != null) {
                        result.putArray("produces", resource.produces);
                    }

                    JsonArray apis = new JsonArray();
                    result.putArray("apis", apis);

                    for (String key : resource.apis.getFieldNames()) {
                        apis.addObject(resource.apis.getObject(key));
                    }
                    result.putObject("models", resource.models);
                    request.response().end(result);
                } else {
                    next.handle(400);
                }
            }
        });

        return resource;
    }
}
