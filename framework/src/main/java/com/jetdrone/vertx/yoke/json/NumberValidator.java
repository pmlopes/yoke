package com.jetdrone.vertx.yoke.json;

final class NumberValidator {

    public static boolean isValid(Object instance, JsonSchemaResolver.Schema schema) {
        if (!isNumber(instance)) {
            return false;
        }

        Number number = (Number) instance;

        // validate divisibleBy
        Number divisibleBy = schema.get("divisibleBy");

        if (divisibleBy != null) {
            if (number == null || number.doubleValue() % divisibleBy.doubleValue() != 0) {
                return false;
            }
        }

        // validate minimum
        Number minimum = schema.get("minimum");
        Boolean exclusiveMinimum = schema.get("exclusiveMinimum");

        if (minimum != null) {
            if (exclusiveMinimum == null) {
                exclusiveMinimum = false;
            }

            if (number == null) {
                return false;
            }

            if (exclusiveMinimum ? (number.doubleValue() <= minimum.doubleValue()) : (number.doubleValue() < minimum.doubleValue())) {
                return false;
            }
        }

        // validate maximum
        Number maximum = schema.get("maximum");
        Boolean exclusiveMaximum = schema.get("exclusiveMaximum");

        if (maximum != null) {
            if (exclusiveMaximum == null) {
                exclusiveMaximum = false;
            }

            if (number == null) {
                return false;
            }

            if (exclusiveMaximum ? (maximum.doubleValue() <= number.doubleValue()) : (maximum.doubleValue() < number.doubleValue())) {
                return false;
            }
        }

        return true;
    }

    private static boolean isNumber(Object value) {
        return value == null || value instanceof Number;
    }

}
