package com.jetdrone.vertx.yoke.json;

import java.util.regex.Pattern;

final class StringValidator {

    private static final Pattern DATETIME = Pattern.compile("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z$");
    private static final Pattern EMAIL = Pattern.compile("^(?:[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+\\.)*[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+@(?:(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!\\.)){0,61}[a-zA-Z0-9]?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!$)){0,61}[a-zA-Z0-9]?)|(?:\\[(?:(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\.){3}(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\]))$");
    private static final Pattern IPV4 = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    private static final Pattern IPV6 = Pattern.compile("^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$");
    private static final Pattern URI = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+-.]*:[^\\s]*$");
    private static final Pattern HOSTNAME = Pattern.compile("^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$");


    public static boolean isValid(Object instance, JsonSchemaResolver.Schema schema) {
        if (!isString(instance)) {
            return false;
        }

        String string = (String) instance;

        // validate minLength
        Integer minLength = schema.get("minLength");

        if (minLength != null) {
            if (string == null || string.length() < minLength) {
                return false;
            }
        }

        // validate maxLength
        Integer maxLength = schema.get("maxLength");

        if (maxLength != null) {
            if (string == null || string.length() > maxLength) {
                return false;
            }
        }

        // validate pattern
        Object pattern = schema.get("pattern");

        if (pattern != null) {
            if (pattern instanceof String) {
                // compile
                pattern = Pattern.compile((String) pattern);
                schema.put("pattern", pattern);
            }
            if (string == null || !((Pattern) pattern).matcher(string).matches()) {
                return false;
            }
        }

        // validate format
        String format = schema.get("format");

        if (format != null) {
            if (string == null) {
                return false;
            }

            switch (format) {
                case "date-time":
                    if (!DATETIME.matcher(string).matches()) {
                        return false;
                    }
                    break;
                case "email":
                    if (!EMAIL.matcher(string).matches()) {
                        return false;
                    }
                    break;
                case "hostname":
                    if (!HOSTNAME.matcher(string).matches()) {
                        return false;
                    }
                    break;
                case "ipv4":
                    if (!IPV4.matcher(string).matches()) {
                        return false;
                    }
                    break;
                case "ipv6":
                    if (!IPV6.matcher(string).matches()) {
                        return false;
                    }
                    break;
                case "uri":
                    if (!URI.matcher(string).matches()) {
                        return false;
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported format: " + format);
            }
        }

        return true;
    }

    private static boolean isString(Object value) {
        return value == null || value instanceof String;
    }

}
