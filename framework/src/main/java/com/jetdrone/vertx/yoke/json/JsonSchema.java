package com.jetdrone.vertx.yoke.json;

import java.util.List;
import java.util.Map;

import com.jetdrone.vertx.yoke.json.JsonSchemaResolver.Schema;

public class JsonSchema {

    public static boolean conformsSchema(Object instance, String schemaRef) {
        return schemaRef == null || conformsSchema(instance, resolve(schemaRef));

    }

    public static boolean conformsSchema(Object instance, Schema schema) {
        if (schema == null) {
            return true;
        }

        if (schema.containsKey("$ref")) {
            return conformsSchema(instance, JsonSchemaResolver.resolveSchema((String) schema.get("$ref"), schema.getParent()));
        }

        final String type = schema.get("type");

        if (isNull(instance)) {
            Object required = schema.get("required");

            if (!(required instanceof Boolean) || Boolean.FALSE.equals(required)) {
                required = null;
            }

            if (required != null) {
                return false;
            }
        }

        switch (type) {
            case "array":
                return validateArray(instance, schema);
            case "string":
                return isString(instance);
            case "number":
                return isNumber(instance);
            case "integer":
                return isInteger(instance);
            case "boolean":
                return isBoolean(instance);
            case "object":
                return validateObject(instance, schema);
            default:
                throw new RuntimeException("Unsupported type: " + type);
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean validateArray(Object value, Schema schema) {
        if (!isArray(value)) {
            return false;
        }

        List<Object> array = (List<Object>) value;
        if (array != null) {
            Object items = schema.get("items");
            Schema itemsSchema = null;

            if (items instanceof Schema) {
                itemsSchema = (Schema) items;
            } else {
                if (items instanceof Map) {
                    // convert to schema
                    itemsSchema = new Schema((Map<String, Object>) items);
                    schema.put("items", itemsSchema);
                }
            }

            setParentIfNotNull(itemsSchema, schema);

            for (Object item : array) {
                if (!conformsSchema(item, itemsSchema)) {
                    return false;
                }
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private static boolean validateObject(Object value, Schema schema) {
        if (!isObject(value)) {
            return false;
        }

        final Map object = (Map) value;
        if (object != null) {

            final List<String> required = schema.get("required");

            if (required != null) {
                for (String field : required) {
                    if (!object.containsKey(field)) {
                        return false;
                    }
                }
            }

            final Map<String, Object> properties = schema.get("properties");

            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    String name = entry.getKey();
                    Object property = entry.getValue();
                    Schema propertySchema = null;

                    if (property instanceof Schema) {
                        propertySchema = (Schema) property;
                    } else {
                        if (property instanceof Map) {
                            // convert to schema
                            propertySchema = new Schema((Map<String, Object>) property);
                            entry.setValue(propertySchema);
                        }
                    }

                    Object item = object.get(name);
                    setParentIfNotNull(propertySchema, schema);

                    if (!conformsSchema(item, propertySchema)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean isString(Object value) {
        return value == null || value instanceof String;
    }

    private static boolean isNumber(Object value) {
        return value == null || value instanceof Number;
    }

    private static boolean isInteger(Object value) {
        return value == null || value instanceof Integer;
    }

    private static boolean isBoolean(Object value) {
        return value == null || value instanceof Boolean;
    }

    private static boolean isArray(Object value) {
        return value == null || value instanceof List;
    }

    private static boolean isObject(Object value) {
        return value == null || value instanceof Map;
    }

    private static boolean isNull(Object value) {
        return value == null;
    }

    private static Schema resolve(String id) {
        return JsonSchemaResolver.resolveSchema(id);
    }

    private static void setParentIfNotNull(Schema schema, Schema parent) {
        if (schema != null) {
            schema.setParent(parent);
        }
    }
}
