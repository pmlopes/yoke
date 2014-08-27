package test.json

import com.jetdrone.vertx.yoke.json.JsonSchemaResolver
import org.junit.Test

import static org.junit.Assert.*

class Resolver {

    @Test
    void testResolveMultipleSchemasInOneFile() {
        def schema

        schema = JsonSchemaResolver.resolveSchema('/schemas/types.json#usage')
        assertNotNull(schema)
        assertEquals("text statement for bank statement", schema.get("description"))
        schema = JsonSchemaResolver.resolveSchema('classpath:///schemas/types.json#usage')
        assertNotNull(schema)
        assertEquals("text statement for bank statement", schema.get("description"))
        schema = JsonSchemaResolver.resolveSchema('classpath:///schemas/types.json')
        assertNotNull(schema)
        assertEquals("custom types", schema.get("description"))

    }
}
