package com.jetdrone.vertx.yoke.test;

import com.jetdrone.vertx.yoke.core.Context;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ContextTest {

    @Test
    public void testContext118() {
        Map<String, Object> ro = new HashMap<String, Object>() {{
            put("title", "ro-title");
        }};

        Context ctx = new Context(ro);
        ctx.put("title", "rw-title");

        Set<String> keys = ctx.keySet();
        Set<String> expectedKeys = new HashSet<String>() {{
            add("title");
        }};

        assertEquals(expectedKeys, keys);

        Collection<Object> values = ctx.values();
        Collection<Object> expectedValues = new HashSet<Object>() {{
            add("ro-title");
            add("rw-title");
        }};

        assertEquals(expectedValues, values);

        Set<Map.Entry<String, Object>> entrySet = ctx.entrySet();
        assertEquals(2, entrySet.size());
    }
}
