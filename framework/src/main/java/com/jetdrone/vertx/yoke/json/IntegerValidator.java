package com.jetdrone.vertx.yoke.json;

final class IntegerValidator {

    public static boolean isValid(Object instance, JsonSchemaResolver.Schema schema) {
        if (!isInteger(instance)) {
            return false;
        }

        Integer number = (Integer) instance;

        // validate divisibleBy
        Integer divisibleBy = schema.get("divisibleBy");

        if (divisibleBy != null) {
            if (number == null || number % divisibleBy != 0) {
                return false;
            }
        }

        // validate minimum
        Integer minimum = schema.get("minimum");
        Boolean exclusiveMinimum = schema.get("exclusiveMinimum");

        if (minimum != null) {
            if (exclusiveMinimum == null) {
                exclusiveMinimum = false;
            }

            if (number == null) {
                return false;
            }

            if (exclusiveMinimum ? (number <= minimum) : (number < minimum)) {
                return false;
            }
        }

        // validate maximum
        Integer maximum = schema.get("maximum");
        Boolean exclusiveMaximum = schema.get("exclusiveMaximum");

        if (maximum != null) {
            if (exclusiveMaximum == null) {
                exclusiveMaximum = false;
            }

            if (number == null) {
                return false;
            }

            if (exclusiveMaximum ? (maximum <= number) : (maximum < number)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isInteger(Object value) {
        return value == null || value instanceof Integer;
    }

}
