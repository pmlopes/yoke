package test.json

import com.jetdrone.vertx.yoke.json.JsonSchema
import com.jetdrone.vertx.yoke.json.JsonSchemaResolver
import org.junit.Test

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class ArraysUniqueItemsTest {

    @Test
    void it_should_validate_if_array_has_no_duplicate_items() {
        assertTrue(JsonSchema.conformsSchema([1], new JsonSchemaResolver.Schema(['type': 'array', 'uniqueItems': true])));
    }

    @Test
    void it_should_validate_if_array_has_no_duplicate_objects() {
        assertTrue(JsonSchema.conformsSchema([1, 2, "1", "2", [a:1], [a:1, b:1]], new JsonSchemaResolver.Schema(['type': 'array', 'uniqueItems': true])));
    }

    @Test
    void it_should_not_validate_if_array_has_duplicate_numbers() {
        assertFalse(JsonSchema.conformsSchema([1, 2, 4, 1, 3, 5], new JsonSchemaResolver.Schema(['type': 'array', 'uniqueItems': true])));
    }

    @Test
    void it_should_not_validate_if_array_has_duplicate_objects() {
        assertFalse(JsonSchema.conformsSchema([[a:1], [a:1]], new JsonSchemaResolver.Schema(['type': 'array', 'uniqueItems': true])));
    }

    @Test
    void it_should_validate_if_not_an_Array() {
        assertTrue(JsonSchema.conformsSchema(null, new JsonSchemaResolver.Schema(['type': 'object', 'uniqueItems': true])));
    }
}
