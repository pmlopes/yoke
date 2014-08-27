package com.jetdrone.vertx.yoke.json;

import org.vertx.java.core.json.JsonObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
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

    private static void tryToLoad(String ref) {
        try {
            JsonObject json;

            final URI uri = new URI(ref);

            final String scheme = uri.getScheme();

            if (scheme != null) {
                // there is a scheme so we can load from http, classpath, or file
                switch (scheme) {
                    case "classpath":
                        json = loadFromClasspath(uri);
                        break;
                    case "http":
                    case "https":
                        json = loadFromURL(uri);
                        break;
                    case "file":
                        json = loadFromFile(uri);
                        break;
                    default:
                        throw new RuntimeException("Unknown Protocol: " + scheme);
                }
            } else {
                // fallback to class loader when no scheme is present
                final String path = uri.getPath();

                if (path != null) {
                    json = loadFromClasspath(uri);
                } else {
                    throw new RuntimeException("Unknown URI: " + ref);
                }
            }

            final Schema schema = new Schema(json, ref);
            final String schemaId = schema.get("id");

            if (schemaId != null) {
                if (loadedSchemas.containsKey(schemaId)) {
                    throw new RuntimeException("Schema ID [" + schemaId + "] already in use!");
                }
                // register the schema into the registry using its Id
                loadedSchemas.put(schemaId, schema);
            }

            if (loadedSchemas.containsKey(ref)) {
                throw new RuntimeException("Schema URI [" + uri + "] already in use!");
            }

            // register the schema into the registry using its URI
            loadedSchemas.put(ref, schema);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonObject loadFromURL(final URI uri) {
        try {
            try (Reader r = new BufferedReader(new InputStreamReader(uri.toURL().openStream(), "UTF-8"))) {

                Writer writer = new StringWriter();

                char[] buffer = new char[1024];
                int n;
                while ((n = r.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

                final JsonObject json = new JsonObject(writer.toString());
                final String fragment = uri.getFragment();
                if (fragment != null) {
                    if (json.containsField(fragment)) {
                        return json.getObject(fragment);
                    } else {
                        throw new RuntimeException("Fragment #" + fragment + " not found!");
                    }
                }

                return json;
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static JsonObject loadFromClasspath(final URI uri) {
        try {
            try (Reader r = new BufferedReader(new InputStreamReader(JsonSchemaResolver.class.getResourceAsStream(uri.getPath()), "UTF-8"))) {

                Writer writer = new StringWriter();

                char[] buffer = new char[1024];
                int n;
                while ((n = r.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

                final JsonObject json = new JsonObject(writer.toString());
                final String fragment = uri.getFragment();
                if (fragment != null) {
                    if (json.containsField(fragment)) {
                        return json.getObject(fragment);
                    } else {
                        throw new RuntimeException("Fragment #" + fragment + " not found!");
                    }
                }

                return json;
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static JsonObject loadFromFile(final URI uri) {
        try {
            try (Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(uri.getPath()), "UTF-8"))) {

                Writer writer = new StringWriter();

                char[] buffer = new char[1024];
                int n;
                while ((n = r.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

                final JsonObject json = new JsonObject(writer.toString());
                final String fragment = uri.getFragment();
                if (fragment != null) {
                    if (json.containsField(fragment)) {
                        return json.getObject(fragment);
                    } else {
                        throw new RuntimeException("Fragment #" + fragment + " not found!");
                    }
                }

                return json;
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
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
