package com.jetdrone.vertx.yoke.json;

import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class ObjectValidator {

    public static boolean isValid(Object instance, JsonSchemaResolver.Schema schema) {
        if (!isObject(instance)) {
            return false;
        }

        // apply default value
        if (instance == null) {
            instance = schema.get("default");
        }

        // from now on work with maps
        if (instance instanceof JsonObject) {
            instance = ((JsonObject) instance).getMap();
        }

        final Map object = (Map) instance;

        // required takes precedence if instance is null
        final List<String> required = schema.get("required");

        if (object == null && required != null && required.size() > 0) {
            return false;
        }

        if (object != null) {
            // validate maxProperties
            Integer maxProperties = schema.get("maxProperties");

            if (maxProperties != null) {
                if (object.keySet().size() > maxProperties) {
                    return false;
                }
            }

            // validate minProperties
            Integer minProperties = schema.get("minProperties");

            if (minProperties != null) {
                if (object.keySet().size() < minProperties) {
                    return false;
                }
            }

            // validate required
            if (required != null) {
                for (String field : required) {
                    if (!object.containsKey(field)) {
                        return false;
                    }
                }
            }

            // TODO: validate additionalProperties

            // validate dependencies
            Map<String, Object> dependencies = schema.get("dependencies");

            if (dependencies != null) {
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

                        if (entry.getValue() instanceof Map) {
                            JsonSchemaResolver.Schema schemaDependency = JsonSchemaResolver.resolveSchema((Map<String, Object>) entry.getValue(), schema.getParent());
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
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    String name = entry.getKey();
                    Object property = entry.getValue();
                    JsonSchemaResolver.Schema propertySchema = null;

                    if (property instanceof JsonSchemaResolver.Schema) {
                        propertySchema = (JsonSchemaResolver.Schema) property;
                    } else {
                        if (property instanceof Map) {
                            // convert to schema
                            propertySchema = JsonSchemaResolver.resolveSchema((Map<String, Object>) property, schema);
                            entry.setValue(propertySchema);
                        }
                    }

                    Object item = object.get(name);
//                    setParentIfNotNull(propertySchema, schema);

                    if (!JsonSchema.conformsSchema(item, propertySchema)) {
                        return false;
                    }
                }
            }

            // validate patternProperties
            final Map<String, Object> patternProperties = schema.get("patternProperties");

            if (patternProperties != null) {
                for (Map.Entry<String, Object> entry : patternProperties.entrySet()) {
                    String name = entry.getKey();
                    Pattern pattern = Pattern.compile(name);
                    Object property = entry.getValue();
                    JsonSchemaResolver.Schema propertySchema = null;

                    if (property instanceof JsonSchemaResolver.Schema) {
                        propertySchema = (JsonSchemaResolver.Schema) property;
                    } else {
                        if (property instanceof Map) {
                            // convert to schema
                            propertySchema = JsonSchemaResolver.resolveSchema((Map<String, Object>) property);
                            entry.setValue(propertySchema);
                        }
                    }

                    for (Object key : object.keySet()) {
                        if(pattern.matcher((String) key).matches()) {
                            Object item = object.get(key);
//                            setParentIfNotNull(propertySchema, schema);

                            if (!JsonSchema.conformsSchema(item, propertySchema)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private static boolean isObject(Object value) {
        return value == null || value instanceof Map || value instanceof JsonObject;
    }
}
