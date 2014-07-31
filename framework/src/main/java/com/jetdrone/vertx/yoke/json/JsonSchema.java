package com.jetdrone.vertx.yoke.json;

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

        if (!AnyValidator.isValid(instance, schema)) {
            return false;
        }

        if (type != null) {
            switch (type) {
                case "null":
                    return isNull(instance);
                case "array":
                    return ArrayValidator.isValid(instance, schema);
                case "string":
                    return StringValidator.isValid(instance, schema);
                case "number":
                    return NumberValidator.isValid(instance, schema);
                case "integer":
                    return IntegerValidator.isValid(instance, schema);
                case "boolean":
                    return isBoolean(instance);
                case "object":
                    return ObjectValidator.isValid(instance, schema);
                default:
                    throw new RuntimeException("Unsupported type: " + type);
            }
        }

        return true;
    }

    private static boolean isBoolean(Object value) {
        return value == null || value instanceof Boolean;
    }

    private static boolean isNull(Object value) {
        return value == null;
    }

    private static Schema resolve(String id) {
        return JsonSchemaResolver.resolveSchema(id);
    }
}
