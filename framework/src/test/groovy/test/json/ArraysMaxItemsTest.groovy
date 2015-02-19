package test.json

import com.jetdrone.vertx.yoke.json.JsonSchema
import com.jetdrone.vertx.yoke.json.JsonSchemaResolver
import org.junit.Test

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class ArraysMaxItemsTest {

    @Test
    void it_should_validate_if_array_has_a_less_than_maxItems() {
        assertTrue(JsonSchema.conformsSchema([1], new JsonSchemaResolver.Schema(['type': 'array', 'items': ['type': 'number'], 'maxItems': 2])));
    }

    @Test
    void it_should_validate_if_array_has_a_length_equal_to_maxItems() {
        assertTrue(JsonSchema.conformsSchema([1, 2], new JsonSchemaResolver.Schema(['type': 'array', 'items': ['type': 'number'], 'maxItems': 2])));
    }

    @Test
    void it_should_not_validate_if_array_has_a_length_larger_than_maxItems () {
        assertFalse(JsonSchema.conformsSchema([1, 2, 3], new JsonSchemaResolver.Schema(['type': 'array', 'items': ['type': 'number'], 'maxItems': 2])));
    }
}
