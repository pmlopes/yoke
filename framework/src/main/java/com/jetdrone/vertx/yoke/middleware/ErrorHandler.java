/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.AbstractMiddleware;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.util.Utils;
import com.jetdrone.vertx.yoke.core.YokeException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * # ErrorHandler
 *
 * Creates pretty print error pages in *html*, *text* or *json* depending on the *accept* header from the client.
 */
public class ErrorHandler extends AbstractMiddleware {

    /**
     * Flag to enable/disable printing the full stack trace of exceptions.
     */
    private final boolean fullStack;

    /**
     * Cached template for rendering the html errors
     */
    private final String errorTemplate;

    /**
     * Create a new ErrorHandler allowing to print or not the stack trace. Include stack trace `true` might be useful in
     * development mode but probably you don't want it in production.
     *
     * <pre>
     * Yoke yoke = new Yoke(...);
     * yoke.use(new ErrorHandler(true);
     * </pre>
     *
     * @param fullStack include full stack trace in error report.
     */
    public ErrorHandler(boolean fullStack) {
        this.fullStack = fullStack;
        errorTemplate = Utils.readResourceToBuffer(getClass(), "error.html").toString();
    }

    /**
     * Override the Middleware isErrorHandler getter.
     *
     * @return always true
     */
    @Override
    public boolean isErrorHandler() {
        return true;
    }

    /**
     * Extracts a single message from a error Object. This will handle Throwables, Strings and Numbers. In case of
     * numbers these are handled as Http error codes.
     *
     * @param error Error object
     * @return String representation of the error object.
     */
    private String getMessage(Object error) {
        if (error instanceof Throwable) {
            String message = ((Throwable) error).getMessage();

            if (message == null) {
                message = "";
            }

            if (fullStack) {
                return error.getClass().getName() + ": " + message;
            } else {
                return message;
            }
        } else if (error instanceof String) {
            return (String) error;
        } else if (error instanceof Integer) {
            return HttpResponseStatus.valueOf((Integer) error).reasonPhrase();
        } else if (error instanceof JsonObject) {
            return ((JsonObject) error).getString("message");
        } else if (error instanceof Map) {
            return (String) ((Map) error).get("message");
        } else {
            return error.toString();
        }
    }

    /**
     * Extracts a single error code from a error Object. This will handle Throwables, Strings and Numbers.
     *
     * @param error Error object
     * @return HTTP status code for the error object
     */
    private int getErrorCode(Object error) {
        if (error instanceof Number) {
            return ((Number) error).intValue();
        } else if (error instanceof YokeException) {
            return ((YokeException) error).getErrorCode().intValue();
        } else if (error instanceof JsonObject) {
            return ((JsonObject) error).getInteger("errorCode", 500);
        } else if (error instanceof Map) {
            Integer tmp = (Integer) ((Map) error).get("errorCode");
            return tmp != null ? tmp : 500;
        } else {
            return 500;
        }
    }

    /**
     * Convert the stack trace to a List in order to be rendered in the error template.
     *
     * @param error error object
     * @return List containing the stack trace for the object
     */
    private List<String> getStackTrace(Object error) {
        if (fullStack && error instanceof Throwable) {
            List<String> stackTrace = new ArrayList<>();
            for (StackTraceElement t : ((Throwable) error).getStackTrace()) {
                stackTrace.add(t.toString());
            }
            return stackTrace;
        } else {
            return Collections.emptyList();
        }
    }

    private boolean sendError(YokeRequest request, String mime, int errorCode, String errorMessage, List<String> stackTrace) {

        YokeResponse response = request.response();

        if (mime.startsWith("text/html")) {
            StringBuilder stack = new StringBuilder();
            for (String s : stackTrace) {
                stack.append("<li>");
                stack.append(s);
                stack.append("</li>");
            }

            response.setContentType("text/html");
            response.end(
                    errorTemplate.replace("{title}", (String) request.get("title"))
                            .replace("{errorCode}", Integer.toString(errorCode))
                            .replace("{errorMessage}", errorMessage)
                            .replace("{stackTrace}", stack.toString())
            );
            return true;
        }

        if (mime.startsWith("application/json")) {
            JsonObject jsonError = new JsonObject();
            jsonError.putObject("error", new JsonObject().putNumber("code", errorCode).putString("message", errorMessage));
            if (!stackTrace.isEmpty()) {
                JsonArray stack = new JsonArray();
                for (String t : stackTrace) {
                    stack.addString(t);
                }
                jsonError.putArray("stack", stack);
            }
            response.setContentType("application/json", "UTF-8");
            response.end(jsonError.encode());
            return true;
        }

        if (mime.startsWith("text/plain")) {
            response.setContentType("text/plain");

            StringBuilder sb = new StringBuilder();
            sb.append("Error ");
            sb.append(errorCode);
            sb.append(": ");
            sb.append(errorMessage);

            for (String t : stackTrace) {
                sb.append("\tat ");
                sb.append(t);
                sb.append("\n");
            }

            response.end(sb.toString());
            return true;
        }

        return false;
    }

    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {

        YokeResponse response = request.response();

        if (response.getStatusCode() < 400) {
            response.setStatusCode(getErrorCode(request.get("error")));
        }

        if (request.get("error") == null) {
            request.put("error", response.getStatusCode());
        }
        String errorMessage = getMessage(request.get("error"));
        int errorCode = response.getStatusCode();

        // set the status message also to the right error code
        response.setStatusMessage(HttpResponseStatus.valueOf(errorCode).reasonPhrase());

        List<String> stackTrace = getStackTrace(request.get("error"));

        // does the response already set the mime type?
        String mime = response.getHeader("content-type");

        if (mime != null) {
            if (sendError(request, mime, errorCode, errorMessage, stackTrace)) {
                return;
            }
        }

        // respect the client accept order
        List<String> acceptedMimes = request.sortedHeader("accept");

        for (String accept : acceptedMimes) {
            if (sendError(request, accept, errorCode, errorMessage, stackTrace)) {
                return;
            }
        }

        // fall back plain/text
        sendError(request, "text/plain", errorCode, errorMessage, stackTrace);
    }
}
