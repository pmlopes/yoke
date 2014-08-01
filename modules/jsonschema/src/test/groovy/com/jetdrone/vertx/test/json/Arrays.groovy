package com.jetdrone.vertx.test.json

import com.jetdrone.vertx.json.JsonSchema
import com.jetdrone.vertx.json.JsonSchemaResolver
import org.junit.Test

import static org.junit.Assert.*

class Arrays {

    @Test
    void it_should_validate_an_empty_array() {
        assertTrue(JsonSchema.conformsSchema([], new JsonSchemaResolver.Schema(['type': 'array', 'items': ['type': 'string']])));
    }

    @Test
    void it_should_validate_an_undefined_array() {
        assertTrue(JsonSchema.conformsSchema(null, new JsonSchemaResolver.Schema(['type': 'array', 'items': ['type': 'string']])));
    }

    @Test
    void it_should_validate_an_array_with_strings() {
        assertTrue(JsonSchema.conformsSchema(['1', '2', '3'], new JsonSchemaResolver.Schema(['type': 'array', 'items': ['type': 'string']])));
    }

    @Test
    void it_should_not_validate_an_array_with_not_all_strings() {
        assertFalse(JsonSchema.conformsSchema(['1', '2', '3', 4], new JsonSchemaResolver.Schema(['type': 'array', 'items': ['type': 'string']])));
    }

    @Test
    void it_should_not_validate_a_non_array() {
        assertFalse(JsonSchema.conformsSchema(0, new JsonSchemaResolver.Schema(['type': 'array'])));
    }
}
