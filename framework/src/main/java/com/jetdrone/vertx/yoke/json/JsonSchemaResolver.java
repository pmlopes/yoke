package com.jetdrone.vertx.yoke.json;

import com.jetdrone.vertx.yoke.util.Utils;
import org.vertx.java.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class JsonSchemaResolver {

    public static class Schema extends HashMap<String, Object> {

        private static final long serialVersionUID = 1l;

        private Schema parent;
        private final String resolvedId;

        public Schema(JsonObject json, String resolvedId) {
            super(json.toMap());
            this.resolvedId = resolvedId;
        }

        public Schema(Map<String, Object> map) {
            super(map);
            this.resolvedId = null;
        }

        public void setParent(Schema parent) {
            this.parent = parent;
        }

        public Schema getParent() {
            return parent;
        }

        public String getResolvedId() {
            return resolvedId;
        }

        @SuppressWarnings("unchecked")
        public <T> T get(String key) {
            return (T) super.get(key);
        }

        public String getLocation() {
            if (parent != null) {
                return parent.getLocation() + "@" + resolvedId;
            }

            return resolvedId;
        }
    }

    private static final Pattern ABSOLUTE = Pattern.compile("^.*://.*");

    private static final Pattern CLASSPATH = Pattern.compile("^classpath://.*");
    private static final Pattern FILE = Pattern.compile("^file://.*");
    private static final Pattern HTTP = Pattern.compile("^http(?:s)?://.*");

    private static Map<String, Schema> loadedSchemas = new HashMap<>();

    public static Schema resolveSchema(String uri) {
        return resolveSchema(uri, null);
    }

    public static Schema resolveSchema(String uri, Schema parentSchema) {
        uri = resolveUri(uri, parentSchema);
        if (!loadedSchemas.containsKey(uri)) {
            tryToLoad(uri);
        }
        return loadedSchemas.get(uri);
    }

    private static String resolveUri(String uri, Schema parent) {
        if (parent == null) {
            return uri;
        }
        String baseUri = parent.getResolvedId();
        if (baseUri == null) {
            baseUri = parent.get("id");
        }
        return resolveRelativeUri(uri, baseUri);
    }


    private static void tryToLoad(String uri) {
        JsonObject json;

        if (CLASSPATH.matcher(uri).matches()) {
            json = new JsonObject(Utils.readResourceToString(JsonSchemaResolver.class, uri.substring(12)));
        } else if (FILE.matcher(uri).matches()) {
            json = new JsonObject(Utils.readFileToString(uri.substring(7)));
        } else if (HTTP.matcher(uri).matches()) {
            json = new JsonObject(Utils.readURLToString(uri));
        } else {
            throw new RuntimeException("Unknown Protocol: " + uri);
        }

        loadedSchemas.put(uri, new Schema(json, uri));
    }

    private static String resolveRelativeUri(String uri, String base) {
        if (base == null || isAbsolute(uri)) {
            return uri;
        }
        if (base.endsWith("/")) {
            return base + uri;
        }
        return base.substring(0, base.lastIndexOf("/") + 1) + uri;
    }

    private static boolean isAbsolute(String uri) {
        return ABSOLUTE.matcher(uri).matches();
    }
}
