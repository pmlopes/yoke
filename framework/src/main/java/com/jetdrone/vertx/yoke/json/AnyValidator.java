package com.jetdrone.vertx.yoke.json;

import java.util.List;

final class AnyValidator {

    public static boolean isValid(Object instance, JsonSchemaResolver.Schema schema) {
        // validate enum
        List<Object> _enum = schema.get("enum");

        Object def = schema.get("default");

        if (def != null && isNull(instance)) {
            instance = def;
        }

        if (_enum != null) {
            if (instance == null || !_enum.contains(instance)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isNull(Object value) {
        return value == null;
    }
}
