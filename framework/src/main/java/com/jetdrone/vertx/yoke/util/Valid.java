package com.jetdrone.vertx.yoke.util;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class Valid {

    private Valid() {}

    public static final Pattern DATETIME = Pattern.compile("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z$");
    public static final Pattern DATE = Pattern.compile("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}$");
    public static final Pattern TIME = Pattern.compile("^\\d{2}:\\d{2}:\\d{2}$");
    public static final Pattern EMAIL = Pattern.compile("^(?:[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+\\.)*[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+@(?:(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!\\.)){0,61}[a-zA-Z0-9]?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!$)){0,61}[a-zA-Z0-9]?)|(?:\\[(?:(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\.){3}(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\]))$");
    public static final Pattern IPADDRESS = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    public static final Pattern IPV6ADDRESS = Pattern.compile("^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$");
    public static final Pattern URI = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+-.]*:[^\\s]*$");
    public static final Pattern HOSTNAME = Pattern.compile("^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$");
    public static final Pattern ALPHA = Pattern.compile("^[a-zA-Z]+$");
    public static final Pattern ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9]+$");

    public static enum is {
        // base json types
        JsonObject,
        JsonArray,
        String,
        Number,
        Boolean,
        Null,
        // specific types
        Integer,
        Long,
        Double,
        // json schema validations
        DateTime,
        Date,
        Time,
        Email,
        IPAddress,
        IPV6Address,
        URI,
        Hostname,
        Alpha,
        Alphanumeric
    }

    private static boolean field(Object field, boolean optional, is what) {
        // null is handled as a special case
        if(field == null) {
            return optional || what == is.Null;
        }

        switch (what) {
            // base json types
            case JsonObject:
                return field instanceof JsonObject || field instanceof Map;
            case JsonArray:
                return field instanceof JsonArray || field instanceof List;
            case String:
                return field instanceof String;
            case Number:
                return field instanceof Number;
            case Boolean:
                return field instanceof Boolean;
            // specific types
            case Integer:
                return field instanceof Integer;
            case Long:
                return field instanceof Long;
            case Double:
                return field instanceof Double;
            // json schema validations
            case DateTime:
                if (field instanceof CharSequence) {
                    return DATETIME.matcher((CharSequence) field).matches();
                }
                break;
            case Date:
                if (field instanceof CharSequence) {
                    return DATE.matcher((CharSequence) field).matches();
                }
                break;
            case Time:
                if (field instanceof CharSequence) {
                    return TIME.matcher((CharSequence) field).matches();
                }
                break;
            case Email:
                if (field instanceof CharSequence) {
                    return EMAIL.matcher((CharSequence) field).matches();
                }
                break;
            case IPAddress:
                if (field instanceof CharSequence) {
                    return IPADDRESS.matcher((CharSequence) field).matches();
                }
                break;
            case IPV6Address:
                if (field instanceof CharSequence) {
                    return IPV6ADDRESS.matcher((CharSequence) field).matches();
                }
                break;
            case URI:
                if (field instanceof CharSequence) {
                    return URI.matcher((CharSequence) field).matches();
                }
                break;
            case Hostname:
                if (field instanceof CharSequence) {
                    return HOSTNAME.matcher((CharSequence) field).matches();
                }
                break;
            case Alpha:
                if (field instanceof CharSequence) {
                    return ALPHA.matcher((CharSequence) field).matches();
                }
                break;
            case Alphanumeric:
                if (field instanceof CharSequence) {
                    return ALPHANUMERIC.matcher((CharSequence) field).matches();
                }
                break;
        }

        // unknown
        return false;
    }

    public static boolean field(Object field, is what) {
        return field(field, false, what);
    }

    public static boolean optionalField(Object field, is what) {
        return field(field, true, what);
    }
}
