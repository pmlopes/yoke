package com.jetdrone.vertx.yoke.util.validation;

import com.jetdrone.vertx.yoke.core.YokeException;
import com.jetdrone.vertx.yoke.core.impl.ThreadLocalUTCDateFormat;
import com.jetdrone.vertx.yoke.json.JsonSchema;
import com.jetdrone.vertx.yoke.json.JsonSchemaResolver;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

public final class That {

    private static final Pattern DATETIME = Pattern.compile("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z$");
    private static final Pattern DATE = Pattern.compile("^\\d{4}-(?:0[0-9]|1[0-2])-[0-9]{2}$");
    private static final Pattern TIME = Pattern.compile("^\\d{2}:\\d{2}:\\d{2}$");
    private static final Pattern EMAIL = Pattern.compile("^(?:[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+\\.)*[\\w!#\\$%&'\\*\\+\\-/=\\?\\^`\\{\\|\\}~]+@(?:(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!\\.)){0,61}[a-zA-Z0-9]?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9\\-](?!$)){0,61}[a-zA-Z0-9]?)|(?:\\[(?:(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\.){3}(?:[01]?\\d{1,2}|2[0-4]\\d|25[0-5])\\]))$");
    private static final Pattern IPADDRESS = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    private static final Pattern IPV6ADDRESS = Pattern.compile("^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$");
    private static final Pattern URI = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+-.]*:[^\\s]*$");
    private static final Pattern HOSTNAME = Pattern.compile("^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$");
    private static final Pattern ALPHA = Pattern.compile("^[a-zA-Z]+$");
    private static final Pattern ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9]+$");

    private static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();
    private static final JsonObject EMPTY = new JsonObject(EMPTY_MAP);

    private static final SimpleNumberComparator NUMBERCOMPARATOR = new SimpleNumberComparator();
    private static final ThreadLocalUTCDateFormat DATEFORMAT = new ThreadLocalUTCDateFormat();

    private static class SimpleNumberComparator implements Comparator<Number>, Serializable {

        private static final long serialVersionUID = 1l;

        @Override
        public int compare(Number o1, Number o2) {
            if (o1 instanceof Short && o2 instanceof Short) {
                return ((Short) o1).compareTo((Short) o2);
            } else if (o1 instanceof Long && o2 instanceof Long) {
                return ((Long) o1).compareTo((Long) o2);
            } else if (o1 instanceof Integer && o2 instanceof Integer) {
                return ((Integer) o1).compareTo((Integer) o2);
            } else if (o1 instanceof Float && o2 instanceof Float) {
                return ((Float) o1).compareTo((Float) o2);
            } else if (o1 instanceof Double && o2 instanceof Double) {
                return ((Double) o1).compareTo((Double) o2);
            } else if (o1 instanceof Byte && o2 instanceof Byte) {
                return ((Byte) o1).compareTo((Byte) o2);
            } else if (o1 instanceof BigInteger && o2 instanceof BigInteger) {
                return ((BigInteger) o1).compareTo((BigInteger) o2);
            } else if (o1 instanceof BigDecimal && o2 instanceof BigDecimal) {
                return ((BigDecimal) o1).compareTo((BigDecimal) o2);
            } else {
                throw new NumberFormatException();
            }
        }
    }

    private final int type;
    private final String path;

    public That(String path) {
        int sep = path.indexOf(":");

        String type;

        // defaults to param
        if (sep == -1) {
            type = "param";
            this.path = path;
        } else {
            type = path.substring(0, sep);
            this.path = path.substring(sep + 1);
        }

        switch (type) {
            case "param":
                this.type = 0;
                break;
            case "form":
                this.type = 1;
                break;
            case "body":
                this.type = 2;
                break;
            case "context":
                this.type = 3;
                break;
            case "header":
                this.type = 4;
                break;
            default:
                throw new RuntimeException("Unknown type: " + type);
        }
    }

    private Object get(final YokeRequest request) throws YokeException {
        switch (type) {
            case 0:
                return request.getParam(isOptional() ? this.path.substring(1) : this.path);
            case 1:
                return request.getFormAttribute(path);
            case 2:
                if (!request.hasBody()) {
                    throw new YokeException(400, "No Body");
                }

                Object obj = request.body();
                if (!(obj instanceof JsonObject)) {
                    throw new YokeException(400, "Body is not JSON");
                }

                JsonObject json = (JsonObject) obj;

                String[] keys = path.split("\\.");

                for (int i = 0; i < keys.length - 1; i++) {
                    boolean optional = keys[i].charAt(0) == '?';

                    if (json == null) {
                        if (optional) {
                            json = EMPTY;
                        } else {
                            throw new YokeException(400, "Parameter '" + keys[i] + "' is not present or is null");
                        }
                    }

                    json = json.getJsonObject(optional ? keys[i].substring(1) : keys[i]);
                }

                boolean optional = keys[keys.length - 1].charAt(0) == '?';

                if (json == null) {
                    if (optional) {
                        json = EMPTY;
                    } else {
                        throw new YokeException(400, "Parameter '" + keys[keys.length - 1] + "' is not present or is null");
                    }
                }

                return json.getValue(optional ? keys[keys.length - 1].substring(1) : keys[keys.length - 1]);
            case 3:
                return request.get(path);
            case 4:
                return request.getHeader(path);
            default:
                throw new YokeException(400, "Unknown source " + type);
        }
    }

    private boolean isOptional() {
        return path.charAt(0) == '?' || path.contains(".?");
    }

    public Assertion is(final Type type) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {

                final Object field = get(request);
                final boolean optional = isOptional();

                // null is handled as a special case
                if (field == null) {
                    if (optional || type == Type.Null) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                switch (type) {
                    case Any:
                        return;
                    // base json types
                    case JsonObject:
                        if (field instanceof JsonObject || field instanceof Map) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case JsonArray:
                        if (field instanceof JsonArray || field instanceof List) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case String:
                        if (field instanceof String) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case Number:
                        if (field instanceof Number) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case Boolean:
                        if (field instanceof Boolean) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                        // specific types
                    case Integer:
                        if (field instanceof Integer) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case Long:
                        if (field instanceof Long) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case Double:
                        if (field instanceof Double) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                        // json schema validations
                    case DateTime:
                        if (field instanceof CharSequence && DATETIME.matcher((CharSequence) field).matches()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case Date:
                        if (field instanceof CharSequence && DATE.matcher((CharSequence) field).matches()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case Time:
                        if (field instanceof CharSequence && TIME.matcher((CharSequence) field).matches()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case Email:
                        if (field instanceof CharSequence && EMAIL.matcher((CharSequence) field).matches()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case IPAddress:
                        if (field instanceof CharSequence && IPADDRESS.matcher((CharSequence) field).matches()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case IPV6Address:
                        if (field instanceof CharSequence && IPV6ADDRESS.matcher((CharSequence) field).matches()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case URI:
                        if (field instanceof CharSequence && URI.matcher((CharSequence) field).matches()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case Hostname:
                        if (field instanceof CharSequence && HOSTNAME.matcher((CharSequence) field).matches()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case Alpha:
                        if (field instanceof CharSequence && ALPHA.matcher((CharSequence) field).matches()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                    case Alphanumeric:
                        if (field instanceof CharSequence && ALPHANUMERIC.matcher((CharSequence) field).matches()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is not " + type.name());
                }

                // unknown
                throw new YokeException(errorCode, "Failed to validate");
            }
        };
    }

    public Assertion exists() {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {

                final Object field = get(request);

                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }
            }
        };
    }

    public Assertion between(final Number min, final Number max) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {

                final Object field = get(request);

                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof String) {
                    int len = ((String) field).length();
                    if (len >= min.intValue() && len <= max.intValue()) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' is outside the range [" + min + ":" + max + "] be NULL");
                }
                if (field instanceof Number) {
                    if (NUMBERCOMPARATOR.compare((Number) field, min) >= 0 && NUMBERCOMPARATOR.compare((Number) field, max) <= 0) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' is outside the range [" + min + ":" + max + "] be NULL");
                }

                if (field instanceof List) {
                    int len = ((List) field).size();
                    if (len >= min.intValue() && len <= max.intValue()) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' is outside the range [" + min + ":" + max + "] be NULL");
                }

                if (field instanceof JsonArray) {
                    int len = ((JsonArray) field).size();
                    if (len >= min.intValue() && len <= max.intValue()) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' is outside the range [" + min + ":" + max + "] be NULL");
                }

                // unknown
                throw new YokeException(errorCode, "Failed to validate");
            }
        };
    }

    public Assertion between(final Date min, final Date max) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {

                final Object field = get(request);

                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof String) {
                    if (DATETIME.matcher((CharSequence) field).matches()) {
                        long millis;
                        try {
                            millis = DATEFORMAT.parse((String) field).getTime();
                        } catch (ParseException e) {
                            throw new YokeException(errorCode, "Failed to validate", e);
                        }
                        if (millis >= min.getTime() && millis <= max.getTime()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is outside the range [" + min + ":" + max + "] be NULL");
                    }
                    if (DATE.matcher((CharSequence) field).matches()) {
                        long millis;
                        try {
                            millis = DATEFORMAT.parse(field + "T00:00:00Z").getTime();
                        } catch (ParseException e) {
                            throw new YokeException(errorCode, "Failed to validate", e);
                        }
                        if (millis >= min.getTime() && millis <= max.getTime()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is outside the range [" + min + ":" + max + "] be NULL");
                    }
                }
                // unknown
                throw new YokeException(errorCode, "Failed to validate");
            }
        };
    }

    public Assertion lessThan(final Number max) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {

                final Object field = get(request);

                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof String) {
                    int len = ((String) field).length();
                    if (len < max.intValue()) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' is outside greater than [" + max + "] be NULL");
                }
                if (field instanceof Number) {
                    if (NUMBERCOMPARATOR.compare((Number) field, max) < 0) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' is outside greater than [" + max + "] be NULL");
                }

                if (field instanceof List) {
                    int len = ((List) field).size();
                    if (len < max.intValue()) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' is outside greater than [" + max + "] be NULL");
                }

                if (field instanceof JsonArray) {
                    int len = ((JsonArray) field).size();
                    if (len < max.intValue()) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' is outside greater than [" + max + "] be NULL");
                }

                // unknown
                throw new YokeException(errorCode, "Failed to validate");
            }
        };
    }

    public Assertion greaterThan(final Number min) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {

                final Object field = get(request);

                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof String) {
                    int len = ((String) field).length();
                    if (len > min.intValue()) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' is less than [" + min + "] be NULL");
                }
                if (field instanceof Number) {
                    if (NUMBERCOMPARATOR.compare((Number) field, min) > 0) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' is less than [" + min + "] be NULL");
                }

                if (field instanceof List) {
                    int len = ((List) field).size();
                    if (len > min.intValue()) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' is less than [" + min + "] be NULL");
                }

                if (field instanceof JsonArray) {
                    int len = ((JsonArray) field).size();
                    if (len > min.intValue()) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' is less than [" + min + "] be NULL");
                }

                // unknown
                throw new YokeException(errorCode, "Failed to validate");
            }
        };
    }

    public Assertion before(final Date max) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {

                final Object field = get(request);

                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof String) {
                    if (DATETIME.matcher((CharSequence) field).matches()) {
                        long millis;
                        try {
                            millis = DATEFORMAT.parse((String) field).getTime();
                        } catch (ParseException e) {
                            throw new YokeException(errorCode, "Failed to validate", e);
                        }
                        if (millis < max.getTime()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is after [" + max + "] be NULL");
                    }
                    if (DATE.matcher((CharSequence) field).matches()) {
                        long millis;
                        try {
                            millis = DATEFORMAT.parse(field + "T00:00:00Z").getTime();
                        } catch (ParseException e) {
                            throw new YokeException(errorCode, "Failed to validate", e);
                        }
                        if (millis < max.getTime()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is after [" + max + "] be NULL");
                    }
                }
                // unknown
                throw new YokeException(errorCode, "Failed to validate");
            }
        };
    }

    public Assertion after(final Date min) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {

                final Object field = get(request);

                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof String) {
                    if (DATETIME.matcher((CharSequence) field).matches()) {
                        long millis;
                        try {
                            millis = DATEFORMAT.parse((String) field).getTime();
                        } catch (ParseException e) {
                            throw new YokeException(errorCode, "Failed to validate", e);
                        }
                        if (millis > min.getTime()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is before [" + min + "] be NULL");
                    }
                    if (DATE.matcher((CharSequence) field).matches()) {
                        long millis;
                        try {
                            millis = DATEFORMAT.parse(field + "T00:00:00Z").getTime();
                        } catch (ParseException e) {
                            throw new YokeException(errorCode, "Failed to validate", e);
                        }
                        if (millis > min.getTime()) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' is before [" + min + "] be NULL");
                    }
                }
                // unknown
                throw new YokeException(errorCode, "Failed to validate");
            }
        };
    }

    public Assertion equals(final String value) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {
                final Object field = get(request);
                final boolean optional = isOptional();

                // null is handled as a special case
                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof String) {
                    if (value.equals(field)) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' does not equal [" + value + "] be NULL");
                }
            }
        };
    }

    public Assertion equals(final Number value) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {
                final Object field = get(request);
                final boolean optional = isOptional();

                // null is handled as a special case
                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof Number) {
                    if (value.equals(field)) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' does not equal [" + value + "] be NULL");
                }
            }
        };
    }

    public Assertion equals(final Date value) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {

                final Object field = get(request);

                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof String) {
                    if (DATETIME.matcher((CharSequence) field).matches()) {
                        Date date;
                        try {
                            date = DATEFORMAT.parse((String) field);
                        } catch (ParseException e) {
                            throw new YokeException(errorCode, "Failed to validate", e);
                        }
                        if (value.equals(date)) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' does not equal [" + value + "] be NULL");
                    }
                    if (DATE.matcher((CharSequence) field).matches()) {
                        Date date;
                        try {
                            date = DATEFORMAT.parse(field + "T00:00:00Z");
                        } catch (ParseException e) {
                            throw new YokeException(errorCode, "Failed to validate", e);
                        }
                        if (value.equals(date)) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' does not equal [" + value + "] be NULL");
                    }
                }
                // unknown
                throw new YokeException(errorCode, "Failed to validate");
            }
        };
    }

    public Assertion equals(final Boolean value) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {
                final Object field = get(request);
                final boolean optional = isOptional();

                // null is handled as a special case
                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof Boolean) {
                    if (value.equals(field)) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' does not equal [" + value + "] be NULL");
                }
            }
        };
    }

    public Assertion notEquals(final String value) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {
                final Object field = get(request);
                final boolean optional = isOptional();

                // null is handled as a special case
                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof String) {
                    if (!value.equals(field)) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' does not equal [" + value + "] be NULL");
                }
            }
        };
    }

    public Assertion notEquals(final Number value) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {
                final Object field = get(request);
                final boolean optional = isOptional();

                // null is handled as a special case
                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof Number) {
                    if (!value.equals(field)) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' does not equal [" + value + "] be NULL");
                }
            }
        };
    }

    public Assertion notEquals(final Date value) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {

                final Object field = get(request);

                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof String) {
                    if (DATETIME.matcher((CharSequence) field).matches()) {
                        Date date;
                        try {
                            date = DATEFORMAT.parse((String) field);
                        } catch (ParseException e) {
                            throw new YokeException(errorCode, "Failed to validate", e);
                        }
                        if (!value.equals(date)) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' does not equal [" + value + "] be NULL");
                    }
                    if (DATE.matcher((CharSequence) field).matches()) {
                        Date date;
                        try {
                            date = DATEFORMAT.parse(field + "T00:00:00Z");
                        } catch (ParseException e) {
                            throw new YokeException(errorCode, "Failed to validate", e);
                        }
                        if (!value.equals(date)) {
                            return;
                        }
                        throw new YokeException(errorCode, "'" + path + "' does not equal [" + value + "] be NULL");
                    }
                }
                // unknown
                throw new YokeException(errorCode, "Failed to validate");
            }
        };
    }

    public Assertion notEquals(final Boolean value) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {
                final Object field = get(request);
                final boolean optional = isOptional();

                // null is handled as a special case
                if (field == null) {
                    throw new YokeException(errorCode, "'" + path + "' cannot be NULL");
                }

                if (field instanceof Boolean) {
                    if (!value.equals(field)) {
                        return;
                    }
                    throw new YokeException(errorCode, "'" + path + "' does not equal [" + value + "] be NULL");
                }
            }
        };
    }

    public Assertion conformsTo(final JsonSchemaResolver.Schema schema) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {
                final Object field = get(request);
                final boolean optional = isOptional();

                // null is handled as a special case
                if (field == null) {
                    throw new YokeException(errorCode, "'" + field + "' cannot be NULL");
                }

                if (JsonSchema.conformsSchema(field, schema)) {
                    return;
                }
                throw new YokeException(errorCode, "'" + field + "' does not conforms to schema");
            }
        };
    }

    public Assertion conformsTo(final String schemaRef) {
        return new Assertion() {
            @Override
            public void ok(final YokeRequest request) throws YokeException {
                final Object field = get(request);
                final boolean optional = isOptional();

                // null is handled as a special case
                if (field == null) {
                    throw new YokeException(errorCode, "'" + field + "' cannot be NULL");
                }

                if (JsonSchema.conformsSchema(field, schemaRef)) {
                    return;
                }
                throw new YokeException(errorCode, "'" + field + "' does not conforms to schema");
            }
        };
    }
}