package com.jetdrone.vertx.test.json

import com.jetdrone.vertx.json.JsonSchema
import com.jetdrone.vertx.json.JsonSchemaResolver
import org.junit.Test

import static org.junit.Assert.*

class Mixed {

    @Test
    void it_should_validate() {
        assertTrue(
                JsonSchema.conformsSchema(
                        ['name': 'test', 'lines': ['1']],
                        new JsonSchemaResolver.Schema([
                                'type'      : 'object',
                                'properties': [
                                        'name' : ['type': 'string'],
                                        'lines': [
                                                'type' : 'array',
                                                'items': ['type': 'string']
                                        ]
                                ]
                        ])));
    }

    @Test
    void it_should_not_validate() {
        assertFalse(
                JsonSchema.conformsSchema(
                        ['name': 'test', 'lines': [1]],
                        new JsonSchemaResolver.Schema([
                                'type'      : 'object',
                                'properties': [
                                        'name' : ['type': 'string'],
                                        'lines': [
                                                'type' : 'array',
                                                'items': ['type': 'string']
                                        ]
                                ]
                        ])));
    }
}
