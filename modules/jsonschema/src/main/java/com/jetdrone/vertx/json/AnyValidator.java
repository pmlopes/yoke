package com.jetdrone.vertx.json;

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

            // validate not
            Object not = schema.get("not");
            if (not != null) {
                if (not instanceof Map) {
                    // convert to schema
                    not = new JsonSchemaResolver.Schema((Map<String, Object>) not);
                    schema.put("not", not);
                }

                if (JsonSchema.conformsSchema(instance, (JsonSchemaResolver.Schema) not)) {
                    return false;
                }
            }
        }

        return true;
    }
}
