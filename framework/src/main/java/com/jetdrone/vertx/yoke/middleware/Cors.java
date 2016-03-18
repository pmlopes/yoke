
package com.jetdrone.vertx.yoke.middleware;

import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_HEADERS;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_METHODS;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_EXPOSE_HEADERS;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_REQUEST_HEADERS;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_REQUEST_METHOD;
import static io.netty.handler.codec.http.HttpHeaders.Names.ORIGIN;
import static io.netty.handler.codec.http.HttpMethod.OPTIONS;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;

import com.jetdrone.vertx.yoke.Middleware;

/**
 * Basic CORS support.
 */
public class Cors extends Middleware
{
    private final Pattern allowedOriginPattern;
    private final Set<String> allowedMethods;
    private final Set<String> allowedHeaders;
    private final Set<String> exposedHeaders;
    private final boolean allowCredentials;

    /**
     * @param allowedOriginPattern if null, '*' will be used.
     */
    public Cors(final Pattern allowedOriginPattern,
                final Set<String> allowedMethods,
                final Set<String> allowedHeaders,
                final Set<String> exposedHeaders,
                final boolean allowCredentials)
    {
        if (allowCredentials && allowedOriginPattern == null)
        {
            throw new IllegalArgumentException("Resource that supports credentials can't accept all origins.");
        }

        this.allowedOriginPattern = allowedOriginPattern;
        this.allowedMethods = allowedMethods;
        this.allowedHeaders = allowedHeaders;
        this.exposedHeaders = exposedHeaders;
        this.allowCredentials = allowCredentials;
    }

    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next)
    {
        if (isPreflightRequest(request))
        {
            handlePreflightRequest(request);
        }
        else
        {
            addCorsResponseHeaders(request, request.response());
            next.handle(null);
        }
    }

    private boolean isPreflightRequest(final YokeRequest request)
    {
        return OPTIONS.name().equals(request.method())
               && (request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS) != null || request.getHeader(ACCESS_CONTROL_REQUEST_METHOD) != null);
    }

    private void handlePreflightRequest(final YokeRequest request)
    {
        if (isValidOrigin(request.getHeader(ORIGIN)))
        {
            addCorsResponseHeaders(request.getHeader(ORIGIN),
                request.response().setStatusCode(204).setStatusMessage("No Content")).end();
        }
        else
        {
            request.response().setStatusCode(403).setStatusMessage("CORS Rejected").end();
        }
    }

    private HttpServerResponse addCorsResponseHeaders(final YokeRequest request, final YokeResponse response)
    {
        final String origin = request.getHeader(ORIGIN);
        return addCorsResponseHeaders(origin, response);
    }

    private HttpServerResponse addCorsResponseHeaders(final String origin, final YokeResponse response)
    {
        if (isValidOrigin(origin))
        {
            response.putHeader(ACCESS_CONTROL_ALLOW_ORIGIN, getAllowedOrigin(origin));

            if (!isEmpty(allowedMethods))
            {
                response.putHeader(ACCESS_CONTROL_ALLOW_METHODS, join(allowedMethods, ","));
            }

            if (!isEmpty(allowedHeaders))
            {
                response.putHeader(ACCESS_CONTROL_ALLOW_HEADERS, join(allowedHeaders, ","));
            }

            if (!isEmpty(exposedHeaders))
            {
                response.putHeader(ACCESS_CONTROL_EXPOSE_HEADERS, join(exposedHeaders, ","));
            }

            if (allowCredentials)
            {
                response.putHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            }
        }

        return response;
    }

    private boolean isValidOrigin(final String origin)
    {
        return allowedOriginPattern == null
               || (isNotBlank(origin) && allowedOriginPattern.matcher(origin).matches());
    }

    private String getAllowedOrigin(final String origin)
    {
        return allowedOriginPattern == null ? "*" : origin;
    }

    private static boolean isEmpty(final Collection<?> c)
    {
        return c == null || c.isEmpty();
    }

    private static boolean isNotBlank(final String s)
    {
        return s != null && !s.trim().isEmpty();
    }

    private static String join(final Collection<String> ss, final String j)
    {
        if (ss == null || ss.isEmpty())
        {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (final String s : ss)
        {
            if (!first)
            {
                sb.append(j);
            }
            sb.append(s);
            first = false;
        }
        return sb.toString();
    }
}
