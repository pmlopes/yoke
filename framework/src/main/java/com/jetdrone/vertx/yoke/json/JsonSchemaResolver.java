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
        private final URI id;

        Schema(Map<String, Object> json) {
            this(json, null);
        }

        Schema(Map<String, Object> json, Schema parent) {
            super(json);

            String id = (String) json.get("id");
            if (id != null) {
                try {
                    if (ABSOLUTE.matcher(id).matches()) {
                        this.id = new URI(id);
                    } else {
                        if (id.charAt(0) == '#') {
                            // this is a fragment, in this case resolve to parent
                            this.id = new URI(parent.id.getScheme(), parent.id.getUserInfo(), parent.id.getHost(), parent.id.getPort(), parent.id.getPath(), parent.id.getQuery(), id.substring(1));
                        } else {
                            // this is a file relative to the parent path
                            File path = new File(new File(parent.id.getPath()).getParent(), id);
                            this.id = new URI(parent.id.getScheme(), parent.id.getUserInfo(), parent.id.getHost(), parent.id.getPort(), path.getPath(), parent.id.getQuery(), "");
                        }
                    }
                }catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            } else {
                this.id = null;
            }
        }

        public void setParent(Schema parent) {
            this.parent = parent;
        }

        public Schema getParent() {
            return parent;
        }

        public String getId() {
            return id != null ? id.toString() : null;
        }

        @SuppressWarnings("unchecked")
        public <T> T get(String key) {
            return (T) super.get(key);
        }
    }

    private static final Pattern ABSOLUTE = Pattern.compile("^.*://.*");

    private static Map<String, Schema> loadedSchemas = new HashMap<>();

    public static Schema resolveSchema(String uri) {
        return resolveSchema(uri, null);
    }

    public static Schema resolveSchema(String uri, Schema parent) {
        uri = resolveUri(uri, parent);
        if (!loadedSchemas.containsKey(uri)) {
            load(uri, parent);
        }
        return loadedSchemas.get(uri);
    }

    public static Schema resolveSchema(Map<String, Object> schema) {
        return new JsonSchemaResolver.Schema(schema, null);
    }

    public static Schema resolveSchema(Map<String, Object> schema, Schema parent) {
        final Schema _schema = new JsonSchemaResolver.Schema(schema, parent);
        _schema.setParent(parent);

        return _schema;
    }

    private static String resolveUri(String uri, Schema parent) {
        // if it is an absolute URI return it, nothing to resolve
        if (ABSOLUTE.matcher(uri).matches()) {
            return uri;
        }

        if (parent == null) {
            throw new RuntimeException("relative URI without a base URI");
        }

        String parentBaseUri;
        int idx = parent.getId().indexOf('#');

        if (idx != -1) {
            parentBaseUri = parent.getId().substring(0, parent.getId().indexOf('#'));
        } else {
            parentBaseUri = parent.getId();
        }

        if (uri.charAt(0) == '#') {
            return parentBaseUri + uri;
        }

        throw new RuntimeException("non relative URI");
    }

    private static void load(String ref, Schema parent) {
        try {
            JsonObject json;

            final URI uri = new URI(ref);

            final String scheme = uri.getScheme();

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

            final String fragment = uri.getFragment();
            if (fragment != null && !"".equals(fragment)) {
                String[] nodes = fragment.split("/");

                for (int i = "".equals(nodes[0]) ? 1 : 0 ; i < nodes.length; i++) {
                    if (json.containsField(nodes[i])) {
                        json = json.getObject(nodes[i]);
                    } else {
                        throw new RuntimeException("Fragment Node #" + nodes[i] + " not found!");
                    }
                }
            }

            final Schema schema = new Schema(json.toMap(), parent);
            final String schemaId = schema.getId();

            if (schemaId != null) {
                if (loadedSchemas.containsKey(schemaId)) {
                    throw new RuntimeException("Schema ID [" + schemaId + "] already in use!");
                }
                // register the schema into the registry using its Id
                loadedSchemas.put(schemaId, schema);
            } else {
                if (loadedSchemas.containsKey(ref)) {
                    throw new RuntimeException("Schema URI [" + uri + "] already in use!");
                }

                // register the schema into the registry using its URI
                loadedSchemas.put(ref, schema);
            }

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

                return new JsonObject(writer.toString());
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static JsonObject loadFromClasspath(final URI uri) {
        try {
            final String path = uri.getPath();
            if (path == null || "".equals(path)) {
                throw new RuntimeException("Invalid path [" + uri.toString() + "]");
            }
            try (Reader r = new BufferedReader(new InputStreamReader(JsonSchemaResolver.class.getResourceAsStream(path), "UTF-8"))) {

                Writer writer = new StringWriter();

                char[] buffer = new char[1024];
                int n;
                while ((n = r.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

                return new JsonObject(writer.toString());
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static JsonObject loadFromFile(final URI uri) {
        try {
            final String path = uri.getPath();
            if (path == null || "".equals(path)) {
                throw new RuntimeException("Invalid path [" + uri.toString() + "]");
            }
            try (Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))) {

                Writer writer = new StringWriter();

                char[] buffer = new char[1024];
                int n;
                while ((n = r.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

                return new JsonObject(writer.toString());
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
