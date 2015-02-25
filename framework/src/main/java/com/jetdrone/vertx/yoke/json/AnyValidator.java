package com.jetdrone.vertx.yoke.json;

import java.util.List;
import java.util.Map;

public final class AnyValidator {

    public static boolean isValid(Object instance, JsonSchemaResolver.Schema schema) {
        // validate required
        if (instance == null) {
            if (Boolean.TRUE.equals(schema.get("required"))) {
                return false;
            }
        }

        // apply default value
        if (instance == null) {
            instance = schema.get("default");
        }

        if (instance != null) {
            // validate enum
            List<Object> _enum = schema.get("enum");
            if (_enum != null && !_enum.contains(instance)) {
                return false;
            }

            // TODO: type

            // validate allOf
            List<Object> allOf = schema.get("allOf");
            if (allOf != null) {
                for (int i = 0; i < allOf.size(); i++) {
                    Object item = allOf.get(i);

                    if (item instanceof Map) {
                        // convert to schema
                        item = JsonSchemaResolver.resolveSchema((Map<String, Object>) item);
                        allOf.set(i, item);
                    }

                    if (!JsonSchema.conformsSchema(instance, (JsonSchemaResolver.Schema) item)) {
                        return false;
                    }
                }
            }

            // validate anyOf
            List<Object> anyOf = schema.get("anyOf");
            if (anyOf != null) {
                boolean match = false;
                for (int i = 0; i < anyOf.size(); i++) {
                    Object item = anyOf.get(i);

                    if (item instanceof Map) {
                        // convert to schema
                        item = JsonSchemaResolver.resolveSchema((Map<String, Object>) item);
                        anyOf.set(i, item);
                    }

                    if (JsonSchema.conformsSchema(instance, (JsonSchemaResolver.Schema) item)) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    return false;
                }
            }

            // validate oneOf
            List<Object> oneOf = schema.get("oneOf");
            if (oneOf != null) {
                int matches = 0;
                for (int i = 0; i < oneOf.size(); i++) {
                    Object item = oneOf.get(i);

                    if (item instanceof Map) {
                        // convert to schema
                        item = JsonSchemaResolver.resolveSchema((Map<String, Object>) item, schema.getParent());
                        oneOf.set(i, item);
                    }

                    if (JsonSchema.conformsSchema(instance, (JsonSchemaResolver.Schema) item)) {
                        matches++;
                    }
                }
                if (matches == 0) {
                    return false;
                }
            }

            // validate not
            Object not = schema.get("not");
            if (not != null) {
                if (not instanceof Map) {
                    // convert to schema
                    not = JsonSchemaResolver.resolveSchema((Map<String, Object>) not, schema.getParent());
                    schema.put("not", not);
                }

                if (JsonSchema.conformsSchema(instance, (JsonSchemaResolver.Schema) not)) {
                    return false;
                }
            }

            // TODO: definitions
        }

        return true;
    }
}
