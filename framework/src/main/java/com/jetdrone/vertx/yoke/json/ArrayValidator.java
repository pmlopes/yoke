package com.jetdrone.vertx.yoke.json;

import io.vertx.core.json.JsonArray;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ArrayValidator {

    public static boolean isValid(Object instance, JsonSchemaResolver.Schema schema) {
        if (!isArray(instance)) {
            return false;
        }

        // apply default value
        if (instance == null) {
            instance = schema.get("default");
        }

        // from now on work with lists
        if (instance instanceof JsonArray) {
            instance = ((JsonArray) instance).getList();
        }

        final List array = (List) instance;

        if (array != null) {
            // validate additionalItems
            Boolean additionalItems = schema.get("additionalItems");

            if (additionalItems != null && !additionalItems) {
                List<Object> items = schema.get("items");
                if (array.size() > items.size()) {
                    return false;
                }
            }

            // validate maxItems
            Integer maxItems = schema.get("maxItems");

            if (maxItems != null) {
                if (array.size() > maxItems) {
                    return false;
                }
            }

            // validate minItems
            Integer minItems = schema.get("minItems");

            if (minItems != null) {
                if (array.size() < minItems) {
                    return false;
                }
            }

            // validate uniqueItems
            Boolean uniqueItems = schema.get("uniqueItems");

            if (uniqueItems != null && uniqueItems) {
                Set<Object> set = new HashSet<>();

                for (Object o : array) {
                    if (!set.add(o)) {
                        return false;
                    }
                }

                set.clear();
            }

            Object items = schema.get("items");
            JsonSchemaResolver.Schema itemsSchema = null;

            if (items instanceof JsonSchemaResolver.Schema) {
                itemsSchema = (JsonSchemaResolver.Schema) items;
            } else {
                if (items instanceof Map) {
                    // convert to schema
                    itemsSchema = JsonSchemaResolver.resolveSchema((Map<String, Object>) items, schema.getParent());
                    schema.put("items", itemsSchema);
                }
            }

//            setParentIfNotNull(itemsSchema, schema);

            for (Object item : array) {
                if (!JsonSchema.conformsSchema(item, itemsSchema)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isArray(Object value) {
        return value == null || value instanceof List || value instanceof JsonArray;
    }

    private static void setParentIfNotNull(JsonSchemaResolver.Schema schema, JsonSchemaResolver.Schema parent) {
        if (schema != null) {
            schema.setParent(parent);
        }
    }
}
