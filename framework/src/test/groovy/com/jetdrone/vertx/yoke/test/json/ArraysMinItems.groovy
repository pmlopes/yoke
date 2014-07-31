package com.jetdrone.vertx.yoke.test.json

import com.jetdrone.vertx.yoke.json.JsonSchema
import com.jetdrone.vertx.yoke.json.JsonSchemaResolver
import org.junit.Test

import static org.junit.Assert.*

class ArraysMinItems {

    @Test
    void it_should_validate_if_array_has_a_length_greater_than_minItems() {
        assertTrue(JsonSchema.conformsSchema([1, 2, 3], new JsonSchemaResolver.Schema(['type': 'array', 'items': ['type': 'number'], 'minItems': 2])));
    }

    @Test
    void it_should_validate_if_array_has_a_length_equal_to_minItems() {
        assertTrue(JsonSchema.conformsSchema([1, 2], new JsonSchemaResolver.Schema(['type': 'array', 'items': ['type': 'number'], 'minItems': 2])));
    }

    @Test
    void it_should_not_validate_if_array_has_a_length_less_than_minItems () {
        assertFalse(JsonSchema.conformsSchema([1], new JsonSchemaResolver.Schema(['type': 'array', 'items': ['type': 'number'], 'minItems': 2])));
    }
}
