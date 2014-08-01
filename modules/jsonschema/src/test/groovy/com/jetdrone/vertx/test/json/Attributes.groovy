package com.jetdrone.vertx.test.json

import com.jetdrone.vertx.json.JsonSchema
import com.jetdrone.vertx.json.JsonSchemaResolver
import org.junit.Test

import static org.junit.Assert.*

class Attributes {

    @Test
    void it_should_validate_a_valid_number() {
        assertTrue(JsonSchema.conformsSchema(0, new JsonSchemaResolver.Schema(['type': 'number'])));
    }

    @Test
    void it_should_not_validate_an_invalid_number() {
        assertFalse(JsonSchema.conformsSchema('0', new JsonSchemaResolver.Schema(['type': 'number'])));
    }

    @Test
    void it_should_not_validate_an_undefined_instance() {
        assertFalse(JsonSchema.conformsSchema(null, new JsonSchemaResolver.Schema(['type': 'number', 'required': true])));
    }

    @Test
    void it_should_validate_null() {
        assertTrue(JsonSchema.conformsSchema(null, new JsonSchemaResolver.Schema(['type': 'null'])));
    }

    @Test
    void it_should_not_validate_no_null() {
        assertFalse(JsonSchema.conformsSchema('0', new JsonSchemaResolver.Schema(['type': 'null'])));
    }

    @Test
    void it_should_validate_integer() {
        assertTrue(JsonSchema.conformsSchema(12, new JsonSchemaResolver.Schema(['type': 'integer'])));
    }

    @Test
    void it_should_not_validate_non_integer() {
        assertFalse(JsonSchema.conformsSchema(0.25, new JsonSchemaResolver.Schema(['type': 'integer'])));
    }

    @Test
    void it_should_not_validate_an_undefined_number_instance() {
        assertFalse(JsonSchema.conformsSchema(null, new JsonSchemaResolver.Schema(['type': 'integer', 'required': true])));
    }

    @Test
    void it_should_validate_true() {
        assertTrue(JsonSchema.conformsSchema(true, new JsonSchemaResolver.Schema(['type': 'boolean'])));
    }

    @Test
    void it_should_validate_false() {
        assertTrue(JsonSchema.conformsSchema(false, new JsonSchemaResolver.Schema(['type': 'boolean'])));
    }

    @Test
    void it_should_not_validate_non_boolean() {
        assertFalse(JsonSchema.conformsSchema('true', new JsonSchemaResolver.Schema(['type': 'boolean'])));
    }

    @Test
    void it_should_not_validate_an_undefined_boolean_instance() {
        assertFalse(JsonSchema.conformsSchema(null, new JsonSchemaResolver.Schema(['type': 'boolean', 'required': true])));
    }

    @Test
    void it_should_validate_if_number_meets_minimum() {
        assertTrue(JsonSchema.conformsSchema(1, new JsonSchemaResolver.Schema(['type': 'number', 'minimum': 1])));
    }

    @Test
    void it_should_not_validate_if_number_is_below_minimum() {
        assertFalse(JsonSchema.conformsSchema(0, new JsonSchemaResolver.Schema(['type': 'number', 'minimum': 1])));
    }

    @Test
    void it_should_validate_if_number_is_above_minimum_using_exclusiveMinimum() {
        assertTrue(JsonSchema.conformsSchema(2, new JsonSchemaResolver.Schema(['type': 'number', 'minimum': 1, 'exclusiveMinimum': true])));
    }

    @Test
    void it_should_not_validate_if_number_is_the_minimum_using_exclusiveMinimum() {
        assertFalse(JsonSchema.conformsSchema(1, new JsonSchemaResolver.Schema(['type': 'number', 'minimum': 1, 'exclusiveMinimum': true])));
    }

    @Test
    void it_should_validate_if_number_is_below_the_maximum() {
        assertTrue(JsonSchema.conformsSchema(1, new JsonSchemaResolver.Schema(['type': 'number', 'maximum': 2])));
    }

    @Test
    void it_should_not_validate_if_number_is_above_maximum() {
        assertFalse(JsonSchema.conformsSchema(3, new JsonSchemaResolver.Schema(['type': 'number', 'maximum': 2])));
    }

    @Test
    void it_should_validate_if_number_is_below_maximum_using_exclusiveMinimum() {
        assertTrue(JsonSchema.conformsSchema(1, new JsonSchemaResolver.Schema(['type': 'number', 'maximum': 2, 'exclusiveMaximum': true])));
    }

    @Test
    void it_should_not_validate_if_number_is_the_maximum_using_exclusiveMinimum() {
        assertFalse(JsonSchema.conformsSchema(2, new JsonSchemaResolver.Schema(['type': 'number', 'maximum': 2, 'exclusiveMaximum': true])));
    }

    @Test
    void it_should_validate_if_number_is_between_minmax() {
        assertTrue(JsonSchema.conformsSchema(1, new JsonSchemaResolver.Schema(['type': 'number', 'minimum': 1, 'maximum': 2])));
    }

    @Test
    void it_should_not_validate_if_number_is_above_minumum() {
        assertFalse(JsonSchema.conformsSchema(3, new JsonSchemaResolver.Schema(['type': 'number', 'minimum': 1, 'maximum': 2])));
    }

    @Test
    void it_should_validate_if_0_is_even() {
        assertTrue(JsonSchema.conformsSchema(2, new JsonSchemaResolver.Schema(['type': 'number', 'divisibleBy': 2])));
    }

    @Test
    void it_should_validate_if_min_2_is_even() {
        assertTrue(JsonSchema.conformsSchema(-2, new JsonSchemaResolver.Schema(['type': 'number', 'divisibleBy': 2])));
    }

    @Test
    void it_should_not_validate_1_is_even() {
        assertFalse(JsonSchema.conformsSchema(1, new JsonSchemaResolver.Schema(['type': 'number', 'divisibleBy': 2])));
    }

    @Test
    void it_should_validate_if_string_matches_the_string_pattern() {
        assertTrue(JsonSchema.conformsSchema('abbbc', new JsonSchemaResolver.Schema(['type': 'string', 'pattern': 'ab+c'])));
    }

    @Test
    void it_should_validate_if_string_does_not_match_the_string_pattern() {
        assertFalse(JsonSchema.conformsSchema('abac', new JsonSchemaResolver.Schema(['type': 'string', 'pattern': 'ab+c'])));
    }

    @Test
    void it_should_validate_if_string_has_a_length_larger_than_minLength() {
        assertTrue(JsonSchema.conformsSchema('abcde', new JsonSchemaResolver.Schema(['type': 'string', 'minLength': 5])));
    }

    @Test
    void it_should_not_validate_if_string_does_has_a_length_less_than_minLength() {
        assertFalse(JsonSchema.conformsSchema('abcde', new JsonSchemaResolver.Schema(['type': 'string', 'minLength': 6])));
    }

    @Test
    void it_should_validate_if_string_has_a_length_equal_to_maxLength() {
        assertTrue(JsonSchema.conformsSchema('abcde', new JsonSchemaResolver.Schema(['type': 'string', 'maxLength': 5])));
    }

    @Test
    void it_should_not_validate_if_string_does_has_a_length_larger_than_maxLength() {
        assertFalse(JsonSchema.conformsSchema('abcde', new JsonSchemaResolver.Schema(['type': 'string', 'maxLength': 4])));
    }

    @Test
    void it_should_validate_if_string_is_one_of_the_enum_values() {
        assertTrue(JsonSchema.conformsSchema('abcde', new JsonSchemaResolver.Schema(['type': 'string', 'enum': ['abcdf', 'abcde']])));
    }

    @Test
    void it_should_not_validate_if_string_is_not_one_of_the_enum_values() {
        assertFalse(JsonSchema.conformsSchema('abcde', new JsonSchemaResolver.Schema(['type': 'string', 'enum': ['abcdf', 'abcdd']])));
    }

    @Test
    void it_should_validate_if_number_is_one_of_the_enum_values() {
        assertTrue(JsonSchema.conformsSchema(1, new JsonSchemaResolver.Schema(['type': 'number', 'enum': [1, 2]])));
    }

    @Test
    void it_should_not_validate_if_number_is_not_one_of_the_enum_values() {
        assertFalse(JsonSchema.conformsSchema(3, new JsonSchemaResolver.Schema(['type': 'string', 'enum': [1, 2]])));
    }

    @Test
    void it_should_validate_if_value_is_undefined_but_defaults_to_one_of_the_enum_values() {
        assertTrue(JsonSchema.conformsSchema(null, new JsonSchemaResolver.Schema(['enum': ['foo', 'bar', 'baz'], 'default': 'baz'])));
    }

    @Test
    void it_should_not_validate_if_value_is_undefined_and_required_even_if_a_default_is_given() {
        assertFalse(JsonSchema.conformsSchema(null, new JsonSchemaResolver.Schema(['enum': ['foo', 'bar', 'baz'], 'required': true, 'default': 'baz'])));
    }

    @Test
    void it_should_not_validate_if_a_required_field_is_ommited() {
        assertFalse(JsonSchema.conformsSchema([:], new JsonSchemaResolver.Schema(['type': 'object', 'properties': ['the_field': ['enum': ['foo', 'bar', 'baz'], 'required': true]]])));
    }

    @Test
    void it_should_not_validate_if_a_required_field_is_undefined() {
        assertFalse(JsonSchema.conformsSchema(['the_field': null], new JsonSchemaResolver.Schema(['type': 'object', 'properties': ['the_field': ['enum': ['foo', 'bar', 'baz'], 'required': true]]])));
    }

    @Test
    void it_should_validate_if_a_required_field_has_a_value_out_of_enum() {
        assertTrue(JsonSchema.conformsSchema(['the_field': 'bar'], new JsonSchemaResolver.Schema(['type': 'object', 'properties': ['the_field': ['enum': ['foo', 'bar', 'baz'], 'required': true]]])));
    }

    @Test
    void it_should_validate_with_missing_non_depended_properties() {
        assertTrue(JsonSchema.conformsSchema([foo: 1], new JsonSchemaResolver.Schema(['type': 'object', 'dependencies': ['quux': ['foo', 'bar']]])));
    }

    @Test
    void it_should_not_validate_with_missing_dependencies() {
        assertFalse(JsonSchema.conformsSchema([quux: 1, foo: 1], new JsonSchemaResolver.Schema(['type': 'object', 'dependencies': ['quux': ['foo', 'bar']]])));
    }

    @Test
    void it_should_validate_with_satisfied_dependencies() {
        assertTrue(JsonSchema.conformsSchema([quux: 1, foo: 1, bar: 1], new JsonSchemaResolver.Schema(['type': 'object', 'dependencies': ['quux': ['foo', 'bar']]])));
    }
}
