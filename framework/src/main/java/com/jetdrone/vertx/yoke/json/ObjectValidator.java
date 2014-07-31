package com.jetdrone.vertx.yoke.json;

import java.util.List;
import java.util.Map;

final class ObjectValidator {

    public static boolean isValid(Object instance, JsonSchemaResolver.Schema schema) {
        if (!isObject(instance)) {
            return false;
        }

        final Map object = (Map) instance;

        // validate maxProperties
        Integer maxProperties = schema.get("maxProperties");

        if (maxProperties != null) {
            if (object == null || object.keySet().size() > maxProperties) {
                return false;
            }
        }

        // validate minProperties
        Integer minProperties = schema.get("minProperties");

        if (minProperties != null) {
            if (object == null || object.keySet().size() < minProperties) {
                return false;
            }
        }

        // validate required
        final List<String> required = schema.get("required");

        if (required != null) {
            if (object == null) {
                return false;
            }

            for (String field : required) {
                if (!object.containsKey(field)) {
                    return false;
                }
            }
        }

        // validate additionalProperties, properties and patternProperties

        // validate dependencies
        Map<String, Object> dependencies = schema.get("dependencies");

        if (dependencies != null) {
            if (object == null) {
                return false;
            }

            for (Map.Entry<String, Object> entry : dependencies.entrySet()) {
                if (object.containsKey(entry.getKey())) {
                    if (entry.getValue() instanceof List) {
                        List<String> propertyDependencies = (List<String>) entry.getValue();
                        for (String propertyDependency : propertyDependencies) {
                            if (!object.containsKey(propertyDependency)) {
                                return false;
                            }
                        }
                    }

                    // the spec does not say this but the official example does...
                    if (entry.getValue() instanceof String) {
                        if (!object.containsKey(entry.getValue())) {
                            return false;
                        }
                    }

                    if (entry.getValue() instanceof Map) {
                        JsonSchemaResolver.Schema schemaDependency = new JsonSchemaResolver.Schema((Map<String, Object>) entry.getValue());
                        if (!JsonSchema.conformsSchema(object.get(entry.getKey()), schemaDependency)) {
                            return false;
                        }
                    }
                }
            }
        }

        // validate properties
        final Map<String, Object> properties = schema.get("properties");

        if (properties != null) {
            if (object == null) {
                return false;
            }
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String name = entry.getKey();
                Object property = entry.getValue();
                JsonSchemaResolver.Schema propertySchema = null;

                if (property instanceof JsonSchemaResolver.Schema) {
                    propertySchema = (JsonSchemaResolver.Schema) property;
                } else {
                    if (property instanceof Map) {
                        // convert to schema
                        propertySchema = new JsonSchemaResolver.Schema((Map<String, Object>) property);
                        entry.setValue(propertySchema);
                    }
                }

                Object item = object.get(name);
                setParentIfNotNull(propertySchema, schema);

                if (!JsonSchema.conformsSchema(item, propertySchema)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isObject(Object value) {
        return value == null || value instanceof Map;
    }

    private static void setParentIfNotNull(JsonSchemaResolver.Schema schema, JsonSchemaResolver.Schema parent) {
        if (schema != null) {
            schema.setParent(parent);
        }
    }
}
