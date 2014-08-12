package com.jetdrone.vertx.yoke.json;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class StringValidator {

    private static final Map<String, Pattern> PATTERNS = new HashMap<>();

    static {
        addPattern("date-time", Pattern.compile("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z$"));
        addPattern("email", Pattern.compile("^(?:[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+\\.)*[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+@(?:(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!\\.)){0,61}[a-zA-Z0-9]?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!$)){0,61}[a-zA-Z0-9]?)|(?:\\[(?:(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\.){3}(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\]))$"));
        addPattern("ipv4", Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"));
        addPattern("ipv6", Pattern.compile("^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$"));
        addPattern("uri", Pattern.compile("^[a-zA-Z][a-zA-Z0-9+-.]*:[^\\s]*$"));
        addPattern("hostname", Pattern.compile("^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$"));
    }

    public static void addPattern(String name, Pattern pattern) {
        PATTERNS.put(name, pattern);
    }

    public static boolean isValid(Object instance, JsonSchemaResolver.Schema schema) {
        if (!isString(instance)) {
            return false;
        }

        // apply default value
        if (instance == null) {
            instance = schema.get("default");
        }

        final String string = (String) instance;

        if (string != null) {
            // validate minLength
            Integer minLength = schema.get("minLength");

            if (minLength != null && string.length() < minLength) {
                return false;
            }

            // validate maxLength
            Integer maxLength = schema.get("maxLength");

            if (maxLength != null && string.length() > maxLength) {
                return false;
            }

            // validate pattern
            Object pattern = schema.get("pattern");

            if (pattern != null) {
                if (pattern instanceof String) {
                    // compile
                    pattern = Pattern.compile((String) pattern);
                    schema.put("pattern", pattern);
                }
                if (!((Pattern) pattern).matcher(string).matches()) {
                    return false;
                }
            }

            // validate format
            String format = schema.get("format");

            if (format != null) {

                Pattern regex = PATTERNS.get(format);

                if (regex != null) {
                    if (!regex.matcher(string).matches()) {
                        return false;
                    }
                } else {
                    throw new RuntimeException("Unsupported format: " + format);
                }
            }
        }

        return true;
    }

    private static boolean isString(Object value) {
        return value == null || value instanceof String;
    }
}
