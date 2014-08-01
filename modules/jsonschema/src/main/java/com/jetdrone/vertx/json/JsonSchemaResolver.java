package com.jetdrone.vertx.json;

import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.json.JsonObject;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class JsonSchemaResolver {

    public static final class Schema extends HashMap<String, Object> {

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
            json = new JsonObject(readResourceToString(JsonSchemaResolver.class, uri.substring(12)));
        } else if (FILE.matcher(uri).matches()) {
            json = new JsonObject(readFileToString(uri.substring(7)));
        } else if (HTTP.matcher(uri).matches()) {
            json = new JsonObject(readURLToString(uri));
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

    /**
     * Avoid using this method for constant reads, use it only for one time only reads from resources in the classpath
     */
    private static String readResourceToString(@NotNull Class<?> clazz, @NotNull String resource) {
        try {
            try (Reader r = new BufferedReader(new InputStreamReader(clazz.getResourceAsStream(resource), "UTF-8"))) {

                Writer writer = new StringWriter();

                char[] buffer = new char[1024];
                int n;
                while ((n = r.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

                return writer.toString();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Avoid using this method for constant reads, use it only for one time only reads from resources in the classpath
     */
    private static String readFileToString(@NotNull String resource) {
        try {
            try (Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(resource), "UTF-8"))) {

                Writer writer = new StringWriter();

                char[] buffer = new char[1024];
                int n;
                while ((n = r.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

                return writer.toString();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Avoid using this method for constant reads, use it only for one time only reads from resources in the classpath
     */
    private static String readURLToString(@NotNull String resource) {
        try {
            try (Reader r = new BufferedReader(new InputStreamReader(new URL(resource).openStream(), "UTF-8"))) {

                Writer writer = new StringWriter();

                char[] buffer = new char[1024];
                int n;
                while ((n = r.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

                return writer.toString();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
