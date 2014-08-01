package com.jetdrone.vertx.yoke.json;

public final class NumberValidator {

    public static boolean isValid(Object instance, JsonSchemaResolver.Schema schema) {
        if (!isNumber(instance)) {
            return false;
        }

        // apply default value
        if (instance == null) {
            instance = schema.get("default");
        }

        final Number number = (Number) instance;

        if (number != null) {
            // validate divisibleBy
            final Number divisibleBy = schema.get("divisibleBy");

            if (divisibleBy != null && number.doubleValue() % divisibleBy.doubleValue() != 0) {
                return false;
            }

            // validate minimum
            final Number minimum = schema.get("minimum");

            if (minimum != null) {
                if (Boolean.TRUE.equals(schema.get("exclusiveMinimum")) ? (number.doubleValue() <= minimum.doubleValue()) : (number.doubleValue() < minimum.doubleValue())) {
                    return false;
                }
            }

            // validate maximum
            final Number maximum = schema.get("maximum");

            if (maximum != null) {
                if (Boolean.TRUE.equals(schema.get("exclusiveMaximum")) ? (maximum.doubleValue() <= number.doubleValue()) : (maximum.doubleValue() < number.doubleValue())) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isNumber(Object value) {
        return value == null || value instanceof Number;
    }
}
