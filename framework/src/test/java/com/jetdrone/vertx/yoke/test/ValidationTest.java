package com.jetdrone.vertx.yoke.test;

import org.junit.Test;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;
import static com.jetdrone.vertx.yoke.util.Valid.*;

public class ValidationTest extends TestVerticle {

    @Test
    public void testNull() {
        Object field = null;
        assertTrue(field(field, is.Null));
        testComplete();
    }

    @Test
    public void testString() {
        Object field = "text";
        assertTrue(field(field, is.String));
        assertTrue(optionalField(null, is.String));
        testComplete();
    }

    @Test
    public void testDateTime() {
        Object field = "2011-10-05T14:48:00.000Z";
        assertTrue(field(field, is.DateTime));
        assertTrue(optionalField(null, is.DateTime));
        testComplete();
    }

    @Test
    public void testDate() {
        Object field = "2011-10-05";
        assertTrue(field(field, is.Date));
        assertTrue(optionalField(null, is.Date));
        testComplete();
    }

    @Test
    public void testTime() {
        Object field = "14:48:00";
        assertTrue(field(field, is.Time));
        assertTrue(optionalField(null, is.Time));
        testComplete();
    }
}
