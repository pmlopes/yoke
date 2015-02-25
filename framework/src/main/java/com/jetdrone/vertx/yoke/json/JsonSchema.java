package com.jetdrone.vertx.yoke.json;

/**
 * JsonSchema validator according to draft-v4
 */
public final class JsonSchema {

    public static boolean conformsSchema(Object instance, String schemaRef) {
        return schemaRef == null || conformsSchema(instance, resolve(schemaRef));
    }

    public static boolean conformsSchema(Object instance, JsonSchemaResolver.Schema schema) {

        if (schema == null) {
            return true;
        }

        if (schema.containsKey("$ref")) {
            return conformsSchema(instance, JsonSchemaResolver.resolveSchema((String) schema.get("$ref"), schema.getParent()));
        }

        final String type = schema.get("type");

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
                    return BooleanValidator.isValid(instance, schema);
                case "object":
                    return ObjectValidator.isValid(instance, schema);
                default:
                    throw new RuntimeException("Unsupported type: " + type);
            }
        }

        return true;
    }

    private static boolean isNull(Object value) {
        return value == null;
    }

    private static JsonSchemaResolver.Schema resolve(String id) {
        return JsonSchemaResolver.resolveSchema(id);
    }
}
