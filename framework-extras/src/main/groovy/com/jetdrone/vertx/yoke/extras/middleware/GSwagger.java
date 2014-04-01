package com.jetdrone.vertx.yoke.extras.middleware;

import com.jetdrone.vertx.yoke.GYoke;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;

public class GSwagger extends Swagger {

    public static class GResource extends Resource {
        GResource(String path, String description) {
            super(path, description);
        }

        public void get(String path, Map<String, Object> operation) {
            super.get(path, new JsonObject(operation));
        }
        public void get(String path, String summary, Map<String, Object> operation) {
            super.get(path, summary, new JsonObject(operation));
        }
        public void post(String path, Map<String, Object> operation) {
            super.post(path, new JsonObject(operation));
        }
        public void post(String path, String summary, Map<String, Object> operation) {
            super.post(path, summary, new JsonObject(operation));
        }
        public void put(String path, Map<String, Object> operation) {
            super.put(path, new JsonObject(operation));
        }
        public void put(String path, String summary, Map<String, Object> operation) {
            super.put(path, summary, new JsonObject(operation));
        }
        public void delete(String path, Map<String, Object> operation) {
            super.delete(path, new JsonObject(operation));
        }
        public void delete(String path, String summary, Map<String, Object> operation) {
            super.delete(path, summary, new JsonObject(operation));
        }

        public GResource addModel(String name, Map<String, Object> model) {
            models.putObject(name, new JsonObject(model));
            return this;
        }
    }

    public GSwagger(String apiVersion) {
        super(apiVersion);
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
    protected GSwagger.GResource createResource(String path, String description) {
        return new GSwagger.GResource(path, description);
    }

    public GSwagger.GResource createResource(GYoke yoke, final String path, final String description) {
        return (GResource) super.createResource(yoke.toJavaYoke(), path, description);
    }

    public GSwagger.GResource createResource(GYoke yoke, final String path) {
        return (GResource) super.createResource(yoke.toJavaYoke(), path);
    }
}
