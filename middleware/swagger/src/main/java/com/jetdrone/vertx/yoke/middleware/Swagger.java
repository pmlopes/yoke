package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.annotations.Processor;
import com.jetdrone.vertx.yoke.annotations.processors.SwaggerProcessor;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: add api_key validator
public class Swagger {

    static {
        Processor.registerProcessor(SwaggerProcessor.class);
    }

    public static class Resource {

        final String path;
        final String description;

        JsonArray produces;
        JsonArray consumes;

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
                if (!produces.contains(mime)) {
                    produces.addString(mime);
                }
            }

            return this;
        }

        public Resource consumes(String... mimes) {
            consumes = new JsonArray();
            for (String mime : mimes) {
                if (!consumes.contains(mime)) {
                    consumes.addString(mime);
                }
            }

            return this;
        }

        public Resource all(String path, JsonObject operation) {
            all(path, "", operation);
            return this;
        }
        public Resource all(String path, String summary, JsonObject operation) {
            addOperation("GET", path, summary, operation);
            addOperation("PUT", path, summary, operation);
            addOperation("POST", path, summary, operation);
            addOperation("DELETE", path, summary, operation);
            addOperation("OPTIONS", path, summary, operation);
            addOperation("HEAD", path, summary, operation);
            addOperation("TRACE", path, summary, operation);
            addOperation("CONNECT", path, summary, operation);
            addOperation("PATCH", path, summary, operation);
            return this;
        }
        public Resource get(String path, JsonObject operation) {
            get(path, "", operation);
            return this;
        }
        public Resource get(String path, String summary, JsonObject operation) {
            addOperation("GET", path, summary, operation);
            return this;
        }
        public Resource post(String path, JsonObject operation) {
            post(path, "", operation);
            return this;
        }
        public Resource post(String path, String summary, JsonObject operation) {
            addOperation("POST", path, summary, operation);
            return this;
        }
        public Resource put(String path, JsonObject operation) {
            put(path, "", operation);
            return this;
        }
        public Resource put(String path, String summary, JsonObject operation) {
            addOperation("PUT", path, summary, operation);
            return this;
        }
        public Resource delete(String path, JsonObject operation) {
            delete(path, "", operation);
            return this;
        }
        public Resource delete(String path, String summary, JsonObject operation) {
            addOperation("DELETE", path, summary, operation);
            return this;
        }
        public Resource options(String path, JsonObject operation) {
            options(path, "", operation);
            return this;
        }
        public Resource options(String path, String summary, JsonObject operation) {
            addOperation("OPTIONS", path, summary, operation);
            return this;
        }
        public Resource head(String path, JsonObject operation) {
            head(path, "", operation);
            return this;
        }
        public Resource head(String path, String summary, JsonObject operation) {
            addOperation("HEAD", path, summary, operation);
            return this;
        }
        public Resource trace(String path, JsonObject operation) {
            trace(path, "", operation);
            return this;
        }
        public Resource trace(String path, String summary, JsonObject operation) {
            addOperation("TRACE", path, summary, operation);
            return this;
        }
        public Resource connect(String path, JsonObject operation) {
            connect(path, "", operation);
            return this;
        }
        public Resource connect(String path, String summary, JsonObject operation) {
            addOperation("CONNECT", path, summary, operation);
            return this;
        }
        public Resource patch(String path, JsonObject operation) {
            patch(path, "", operation);
            return this;
        }
        public Resource patch(String path, String summary, JsonObject operation) {
            addOperation("PATCH", path, summary, operation);
            return this;
        }

        public Resource addModel(String id, JsonObject model) {
            model.putString("id", id);
            models.putObject(id, model);
            return this;
        }

        protected void addOperation(String verb, String path, String summary, JsonObject operation) {
            // translate from yoke format to swagger format
            Matcher m =  Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)").matcher(path);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, "\\{$1\\}");
            }
            m.appendTail(sb);

            JsonObject op = new JsonObject()
                    .putString("method", verb)
                    .putString("summary", summary);

            op.mergeIn(operation);

            JsonObject api = getApi(sb.toString());
            api.getArray("operations").addObject(op);
        }
    }

    private final List<Resource> resources = new ArrayList<>();

    private final Router router;
    private final String prefix;
    private final String apiVersion;

    private JsonObject info;
    private JsonObject authorizations;

    public Swagger(final Router router, final String apiVersion) {
        this(router, "/", apiVersion);
    }

    public Swagger(final Router router, final String prefix, final String apiVersion) {
        this.router = router;
        this.apiVersion = apiVersion;

        if (prefix.endsWith("/")) {
            this.prefix = prefix + "api-docs";
        } else {
            this.prefix = prefix + "/api-docs";
        }

        this.router.get(this.prefix, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
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
            }
        });
    }

    public Swagger setInfo(JsonObject info) {
        this.info = info;
        return this;
    }

    public Swagger setAuthorizations(JsonObject authorizations) {
        this.authorizations = authorizations;
        return this;
    }

    protected Resource createSwaggerResource(final String path, final String description) {
        // verify if already present
        for (Resource res : resources) {
            if (res.path.equals(path)) {
                return res;
            }
        }

        final Resource resource = new Resource(path, description);
        resources.add(resource);

        router.get(prefix + path, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                JsonObject result = new JsonObject()
                        .putString("apiVersion", apiVersion)
                        .putString("swaggerVersion", "1.2")
                        .putString("basePath", "/")
                        .putString("resourcePath", path);

                if (resource.produces != null) {
                    result.putArray("produces", resource.produces);
                }

                if (resource.consumes != null) {
                    result.putArray("consumes", resource.consumes);
                }

                JsonArray apis = new JsonArray();
                result.putArray("apis", apis);

                for (String key : resource.apis.getFieldNames()) {
                    apis.addObject(resource.apis.getObject(key));
                }
                result.putObject("models", resource.models);
                request.response().end(result);
            }
        });

        return resource;
    }

    public Resource createResource(final String path) {
        return createResource(path, "");
    }

    public Resource createResource(final String path, final String description) {

        final String normalizedPath;

        if (path.charAt(0) != '/') {
            normalizedPath = "/" + path;
        } else {
            normalizedPath = path;
        }

        return createSwaggerResource(normalizedPath, description);
    }

    public static Swagger from(final Router router, final String version, final Object... objs) {
        final Swagger swagger = new Swagger(router, version);
        from(swagger, objs);

        return swagger;
    }

    /**
     * Builds a Swagger from an annotated Java Object
     */
    public static Swagger from(final Swagger router, final Object... objs) {
        for (Object o : objs) {
            Processor.process(router, o);
        }

        return router;
    }
}
