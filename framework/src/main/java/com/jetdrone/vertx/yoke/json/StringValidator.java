package com.jetdrone.vertx.yoke.json;

import java.util.regex.Pattern;

final class StringValidator {

    public static boolean isValid(Object instance, JsonSchemaResolver.Schema schema) {
        if (!isString(instance)) {
            return false;
        }

        String string = (String) instance;

        // validate minLength
        Integer minLength = schema.get("minLength");

        if (minLength != null) {
            if (string == null || string.length() < minLength) {
                return false;
            }
        }

        // validate maxLength
        Integer maxLength = schema.get("maxLength");

        if (maxLength != null) {
            if (string == null || string.length() > maxLength) {
                return false;
            }
        }

        // validate pattern
        Object pattern = schema.get("pattern");

        if (pattern != null) {
            if (pattern instanceof String) {
                // compile
                pattern = Pattern.compile((String) pattern);
                schema.put("pattern", pattern);
            }
            if (string == null || !((Pattern) pattern).matcher(string).matches()) {
                return false;
            }
        }

        return true;
    }

    private static boolean isString(Object value) {
        return value == null || value instanceof String;
    }

}
