package com.jetdrone.vertx.yoke.json;

public final class IntegerValidator {

    public static boolean isValid(Object instance, JsonSchemaResolver.Schema schema) {
        if (!isInteger(instance)) {
            return false;
        }

        // apply default value
        if (instance == null) {
            instance = schema.get("default");
        }

        final Integer number = (Integer) instance;

        if (number != null) {
            // validate divisibleBy
            final Integer divisibleBy = schema.get("divisibleBy");

            if (divisibleBy != null && number % divisibleBy != 0) {
                return false;
            }

            // validate minimum
            final Integer minimum = schema.get("minimum");

            if (minimum != null) {
                if (Boolean.TRUE.equals(schema.get("exclusiveMinimum")) ? (number <= minimum) : (number < minimum)) {
                    return false;
                }
            }

            // validate maximum
            final Integer maximum = schema.get("maximum");

            if (maximum != null) {
                if (Boolean.TRUE.equals(schema.get("exclusiveMaximum")) ? (maximum <= number) : (maximum < number)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isInteger(Object value) {
        return value == null || value instanceof Integer;
    }
}
