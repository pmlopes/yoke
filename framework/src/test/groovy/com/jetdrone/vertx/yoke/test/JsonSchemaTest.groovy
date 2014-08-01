package com.jetdrone.vertx.yoke.test

import com.jetdrone.vertx.yoke.json.JsonSchema
import com.jetdrone.vertx.yoke.json.StringValidator
import org.junit.Test
import org.vertx.java.core.json.JsonObject

import java.util.regex.Pattern

import static org.junit.Assert.assertTrue

public class JsonSchemaTest {

    @Test
    public void testJsonSchemaMinimum() {
        // current schema does not specify phone format anymore
        // so we add one for the sake of it
        StringValidator.addPattern("phone", Pattern.compile(".*"));
        StringValidator.addPattern("date", Pattern.compile(".*"));

        JsonObject json = new JsonObject()
                .putString("givenName", "Paulo")
                .putString("familyName", "Lopes");

        assertTrue(JsonSchema.conformsSchema(json.toMap(), "classpath:///card.json"));
    }

    @Test
    public void testJsonSchemaFull() {
        // current schema does not specify phone format anymore
        // so we add one for the sake of it
        StringValidator.addPattern("phone", Pattern.compile(".*"));
        StringValidator.addPattern("date", Pattern.compile(".*"));

        def json = [
                fn             : 'Paulo Lopes',
                familyName     : 'Paulo',
                givenName      : 'Lopes',
                additionalName : ['Manuel'],
                honorificPrefix: [],
                honorificSuffic: [],
                nickname       : 'jetdrone',
                url            : 'http://www.jetdrone.com',
                email          : [
                        type : 'work',
                        value: 'myemail@emailserver.com'
                ],
                tel            : [
                        type : 'home',
                        value: '+31123456789'
                ],
                adr            : [
                        'street-address': 'street 1',
                        'post-office-box': 'pobox1',
                        'extended-address': 'ext1',
                        locality: 'Amsterdam',
                        region: 'North Holland',
                        'postal-code': '1000EX',
                        'country-name': 'The Netherlands'
                ],
                geo            : [
                        latitude:0.0,
                        longitude: 0.0
                ],
                tz             : 'CET',
                photo          : 'paulo.png',
                logo           : 'paulo.logo.png',
                sound          : 'paulo.sound.wav',
                role           : 'developer',
                org            : [
                        organizationName: 'jetdrone',
                        organizationUnit: 'rd1'
                ]
        ];

        assertTrue(JsonSchema.conformsSchema(json, "classpath:///card.json"));
    }
}
