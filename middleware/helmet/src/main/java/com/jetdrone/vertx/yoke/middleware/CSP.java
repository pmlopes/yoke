package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.impl.WebClient;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;

public class CSP extends Middleware {

    private static final List<String> ALL_HEADERS = Arrays.asList(
            "X-Content-Security-Policy",
            "Content-Security-Policy",
            "X-WebKit-CSP"
    );

    private static final List<String> DIRECTIVES = Arrays.asList(
            "default-src",
            "script-src",
            "object-src",
            "img-src",
            "media-src",
            "frame-src",
            "font-src",
            "connect-src",
            "style-src",
            "report-uri",
            "sandbox"
    );

    private static final List<String> MUST_BE_QUOTED = Arrays.asList(
            "none",
            "self",
            "unsafe-inline",
            "unsafe-eval"
    );

    private final JsonObject options;
    private final boolean reportOnly;
    private final boolean setAllHeaders;
    private final boolean safari5;

    public CSP() {
        this(new JsonObject().put("default-src", new JsonArray().add("'self'")));
    }

    public CSP(JsonObject options) {
        this.options = options;
        reportOnly = options.getBoolean("reportOnly", false);
        setAllHeaders = options.getBoolean("setAllHeaders", false);
        safari5 = options.getBoolean("safari5", false);

        Set<String> keys = options.fieldNames();

        for (String key : keys) {
            Object value = options.getValue(key);
            if (value instanceof JsonArray) {
                for (String must : MUST_BE_QUOTED) {
                    if (!((JsonArray) value).contains(must)) {
                        throw new RuntimeException(value + " must be quoted");
                    }
                }
            } else {
                for (String must : MUST_BE_QUOTED) {
                    if (must.equals(value)) {
                        throw new RuntimeException(value + " must be quoted");
                    }
                }
            }
        }

        if (reportOnly && options.getString("report-uri") == null) {
            throw new RuntimeException("Please remove reportOnly or add a report-uri.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {

        List<String> headers = new ArrayList<>();
        Map<String, Object> policy = new HashMap<>();
        boolean setAllHeaders = this.setAllHeaders;

        final WebClient webClient = WebClient.detect(request.getHeader("user-agent"));

        WebClient.UserAgent userAgent = webClient.getUserAgent();
        int version = webClient.getMajorVersion();

        for (String directive : DIRECTIVES) {
            Object value = options.getValue(directive);
            if (value != null) {
                policy.put(directive, value);
            }

            boolean shouldWrapInArray =
                    value instanceof String && !"sandbox".equals(directive) ||
                    "sandbox".equals(directive) && !Boolean.TRUE.equals(value);

            if (shouldWrapInArray) {
                policy.put(directive, value != null ? value.toString().split("\\s") : new String[0]);
            }

        }

        switch (userAgent) {

            case IE:
                if (version >= 10) {
                    headers.add("X-Content-Security-Policy");
                    if (policy.get("sandbox") == null) {
                        policy.put("sandbox", Boolean.TRUE);
                    }
                }
                break;

            case FIREFOX:

                if (version >= 23) {

                    headers.add("Content-Security-Policy");

                } else if ((version >= 4) && (version < 23)) {

                    headers.add("X-Content-Security-Policy");

                    if (policy.get("default-src") == null) {
                        policy.put("default-src", Arrays.asList("*"));
                    }

                    final Set<String> keys = options.fieldNames();

                    for (String key : keys) {

                        Object value = options.getValue(key);

                        if ("connect-src".equals(key)) {
                            policy.put("xhr-src", value);
                        } else if ("default-src".equals(key)) {
                            if (version < 5) {
                                policy.put("allow", value);
                            } else {
                                policy.put("default-src", value);
                            }
                        } else if (!"sandbox".equals(key)) {
                            policy.put(key, value);
                        }

                        if (policy.get(key) instanceof List) {

                            final List<Object> list = (List<Object>) policy.get(key);

                            int index;
                            if ((index = list.indexOf("'unsafe-inline'")) != -1) {
                                if ("script-src".equals(key)) {
                                    list.set(index, "'inline-script'");
                                } else {
                                    list.remove(index);
                                }
                            }
                            if ((index = list.indexOf("'unsafe-eval'")) != -1) {
                                if ("script-src".equals(key)) {
                                    list.set(index, "'eval-script'");
                                } else {
                                    list.remove(index);
                                }
                            }
                        }
                    }
                }

                break;

            case CHROME:
                if ((version >= 14) && (version < 25)) {
                    headers.add("X-WebKit-CSP");
                } else if (version >= 25) {
                    headers.add("Content-Security-Policy");
                }
                break;

            case SAFARI:
                if (version >= 7) {
                    headers.add("Content-Security-Policy");
                } else if ((version >= 6) || ((version >= 5.1) && safari5)) {
                    headers.add("X-WebKit-CSP");
                }
                break;

            case OPERA:
                if (version >= 15) {
                    headers.add("Content-Security-Policy");
                }
                break;

            case CHROME_MOBILE:
                if (version >= 14) {
                    headers.add("Content-Security-Policy");
                }
                break;

            default:
                setAllHeaders = true;

        }

        StringBuilder policyString = new StringBuilder();

        for (Map.Entry<String, Object> entry: policy.entrySet()) {
            if (("sandbox".equals(entry.getKey())) && (Boolean.TRUE.equals(entry.getValue()))) {
                policyString.append("sandbox;");
            } else if (entry.getValue() instanceof List) {
                policyString.append(entry.getKey());
                policyString.append(" ");
                for (Object item : (List) entry.getValue()) {
                    policyString.append(item);
                    policyString.append(" ");
                }
                // remove trailing " "
                policyString.setLength(policyString.length() - 1);
            } else {
                policyString.append(entry.getKey());
                policyString.append(" ");
                policyString.append(entry.getValue());
                policyString.append(";");
            }
        }

        if (policyString.length() > 0) {
            // remove trailing ;
            policyString.setLength(policyString.length() - 1);
        }

        if (setAllHeaders) {
            headers = ALL_HEADERS;
        }

        for (String header : headers) {
            if (reportOnly) {
                header += "-Report-Only";
            }
            request.response().putHeader(header, policyString.toString());
        }

        next.handle(null);
    }
}
