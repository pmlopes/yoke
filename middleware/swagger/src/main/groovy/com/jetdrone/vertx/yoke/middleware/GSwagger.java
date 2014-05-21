package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.annotations.Processor;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;

public class GSwagger extends Swagger {

    public static class GResource extends Resource {
        GResource(String path, String description) {
            super(path, description);
        }

        GResource(Resource parent) {
            super(parent.path, parent.description);

            super.produces = parent.produces;
            super.consumes = parent.consumes;

            super.models = parent.models;
            super.apis = parent.models;
        }

        public GResource get(String path, Map<String, Object> operation) {
            super.get(path, new JsonObject(operation));
            return this;
        }
        public GResource get(String path, String summary, Map<String, Object> operation) {
            super.get(path, summary, new JsonObject(operation));
            return this;
        }
        public GResource post(String path, Map<String, Object> operation) {
            super.post(path, new JsonObject(operation));
            return this;
        }
        public GResource post(String path, String summary, Map<String, Object> operation) {
            super.post(path, summary, new JsonObject(operation));
            return this;
        }
        public GResource put(String path, Map<String, Object> operation) {
            super.put(path, new JsonObject(operation));
            return this;
        }
        public GResource put(String path, String summary, Map<String, Object> operation) {
            super.put(path, summary, new JsonObject(operation));
            return this;
        }
        public GResource delete(String path, Map<String, Object> operation) {
            super.delete(path, new JsonObject(operation));
            return this;
        }
        public GResource delete(String path, String summary, Map<String, Object> operation) {
            super.delete(path, summary, new JsonObject(operation));
            return this;
        }
        public GResource options(String path, Map<String, Object> operation) {
            super.options(path, new JsonObject(operation));
            return this;
        }
        public GResource options(String path, String summary, Map<String, Object> operation) {
            super.options(path, summary, new JsonObject(operation));
            return this;
        }
        public GResource trace(String path, Map<String, Object> operation) {
            super.trace(path, new JsonObject(operation));
            return this;
        }
        public GResource trace(String path, String summary, Map<String, Object> operation) {
            super.trace(path, summary, new JsonObject(operation));
            return this;
        }
        public GResource head(String path, Map<String, Object> operation) {
            super.head(path, new JsonObject(operation));
            return this;
        }
        public GResource head(String path, String summary, Map<String, Object> operation) {
            super.head(path, summary, new JsonObject(operation));
            return this;
        }
        public GResource connect(String path, Map<String, Object> operation) {
            super.connect(path, new JsonObject(operation));
            return this;
        }
        public GResource connect(String path, String summary, Map<String, Object> operation) {
            super.connect(path, summary, new JsonObject(operation));
            return this;
        }
        public GResource patch(String path, Map<String, Object> operation) {
            super.patch(path, new JsonObject(operation));
            return this;
        }
        public GResource patch(String path, String summary, Map<String, Object> operation) {
            super.patch(path, summary, new JsonObject(operation));
            return this;
        }

        public GResource produces(String... mimes) {
            super.produces(mimes);
            return this;
        }

        public GResource consumes(String... mimes) {
            super.consumes(mimes);
            return this;
        }

        public GResource addModel(String name, Map<String, Object> model) {
            super.addModel(name, new JsonObject(model));
            return this;
        }
    }

    public GSwagger(GRouter router, String apiVersion) {
        super(router.toJavaRouter(), apiVersion);
    }

    public GSwagger(GRouter router, String prefix, String apiVersion) {
        super(router.toJavaRouter(), prefix, apiVersion);
    }

    public GSwagger setInfo(Map<String, Object> info) {
        super.setInfo(new JsonObject(info));
        return this;
    }

    public GSwagger setAuthorizations(Map<String, Object> authorizations) {
        super.setAuthorizations(new JsonObject(authorizations));
        return this;
    }

    @Override
    protected GResource createSwaggerResource(String path, String description) {

        final Resource res = super.createSwaggerResource(path, description);

        if (res instanceof GResource) {
            return (GResource) res;
        } else {
            return new GResource(res);
        }
    }

    public GResource createResource(final String path, final String description) {
        final String normalizedPath;

        if (path.charAt(0) != '/') {
            normalizedPath = "/" + path;
        } else {
            normalizedPath = path;
        }

        return createSwaggerResource(normalizedPath, description);
    }

    public GResource createResource(final String path) {
        return createResource(path, "");
    }

    public static GSwagger from(final GRouter router, final String version, final Object... objs) {
        final GSwagger swagger = new GSwagger(router, version);
        from(swagger, objs);

        return swagger;
    }

    /**
     * Builds a Swagger from an annotated Java Object
     */
    public static GSwagger from(final GSwagger router, final Object... objs) {
        for (Object o : objs) {
            Processor.process(router, o);
        }

        return router;
    }
}
