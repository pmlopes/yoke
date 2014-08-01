package com.jetdrone.vertx.yoke.json;

import java.util.List;

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
        }

        return true;
    }
}
