package com.jetdrone.vertx.yoke.test.json

import com.jetdrone.vertx.yoke.json.JsonSchema
import com.jetdrone.vertx.yoke.json.JsonSchemaResolver
import org.junit.Test

import static org.junit.Assert.*

class Formats {

    @Test void it_should_validate_a_valid_date_time() {
        assertTrue(JsonSchema.conformsSchema("2012-07-08T16:41:41.532Z", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'date-time'])));
    }

    @Test void it_should_validate_a_valid_date_time_without_milliseconds() {
        assertTrue(JsonSchema.conformsSchema("2012-07-08T16:41:41Z", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'date-time'])));
    }

    @Test void it_should_not_validate_a_date_time_with_the_time_missing() {
        assertFalse(JsonSchema.conformsSchema("2012-07-08", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'date-time'])));
    }

    @Test void it_should_not_validate_an_invalid_date_time() {
        assertFalse(JsonSchema.conformsSchema("TEST2012-07-08T16:41:41.532Z", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'date-time'])));
    }

    @Test void it_should_validate_www_google_com() {
        assertTrue(JsonSchema.conformsSchema("http://www.google.com/", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'uri'])));
    }

    @Test void it_should_validate_www_google_com_search() {
        assertTrue(JsonSchema.conformsSchema("http://www.google.com/search", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'uri'])));
    }

    @Test void it_should_not_validate_relative_URIs() {
        assertFalse(JsonSchema.conformsSchema("tdegrunt", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'uri'])));
    }

    @Test void it_should_not_validate_with_whitespace() {
        assertFalse(JsonSchema.conformsSchema("The dog jumped", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'uri'])));
    }

    @Test void it_should_validate_obama_at_whitehouse_gov() {
        assertTrue(JsonSchema.conformsSchema("obama@whitehouse.gov", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'email'])));
    }

    @Test void it_should_validate_barack_obama_at_whitehouse_gov() {
        assertTrue(JsonSchema.conformsSchema("barack+obama@whitehouse.gov", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'email'])));
    }

    @Test void it_should_not_validate_obama_at() {
        assertFalse(JsonSchema.conformsSchema("obama@", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'email'])));
    }

    @Test void it_should_validate_192_168_0_1() {
        assertTrue(JsonSchema.conformsSchema("192.168.0.1", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'ipv4'])));
    }

    @Test void it_should_validate_127_0_0_1() {
        assertTrue(JsonSchema.conformsSchema("127.0.0.1", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'ipv4'])));
    }

    @Test void it_should_not_validate_192_168_0() {
        assertFalse(JsonSchema.conformsSchema("192.168.0", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'ipv4'])));
    }

    @Test void it_should_not_validate_256_168_0() {
        assertFalse(JsonSchema.conformsSchema("256.168.0", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'ipv4'])));
    }

    @Test void it_should_validate_fe80__1_lo0() {
        assertTrue(JsonSchema.conformsSchema("fe80::1%lo0", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'ipv6'])));
    }

    @Test void it_should_validate___1() {
        assertTrue(JsonSchema.conformsSchema("::1", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'ipv6'])));
    }

    @Test void it_should_not_validate_127_0_0_1() {
        assertFalse(JsonSchema.conformsSchema("127.0.0.1", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'ipv6'])));
    }

    @Test void it_should_not_validate_localhost() {
        assertFalse(JsonSchema.conformsSchema("localhost", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'ipv6'])));
    }

    @Test void it_should_validate_localhost() {
        assertTrue(JsonSchema.conformsSchema("localhost", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'hostname'])));
    }

    @Test void it_should_validate_www_google_com2() {
        assertTrue(JsonSchema.conformsSchema("www.google.com", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'hostname'])));
    }

    @Test void it_should_not_validate_www__hi__com() {
        assertFalse(JsonSchema.conformsSchema("www.-hi-.com", new JsonSchemaResolver.Schema(['type': 'string', 'format': 'hostname'])));
    }
}
