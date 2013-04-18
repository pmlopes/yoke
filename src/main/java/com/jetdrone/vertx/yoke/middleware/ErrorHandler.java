package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ErrorHandler extends Middleware {

    private final boolean fullStack;
    private String errorTemplate;

    public ErrorHandler(boolean fullStack) {
        this.fullStack = fullStack;
    }

    @Override
    public boolean isErrorHandler() {
        return true;
    }

    private String getMessage(Object error) {
        if (error instanceof Throwable) {
            return ((Throwable) error).getMessage();
        } else if (error instanceof String) {
            return (String) error;
        } else if (error instanceof Integer) {
            return HttpResponseStatus.valueOf((Integer) error).reasonPhrase();
        } else {
            return error.toString();
        }
    }

    private int getErrorCode(Object error) {
        if (error instanceof Integer) {
            return (Integer) error;
        } else {
            return 500;
        }
    }

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

    @Override
    public void setVertx(Vertx vertx) {
        try {
            super.setVertx(vertx);
            Buffer buf = vertx.fileSystem().readFileSync(getClass().getResource("error.html").getPath());
            errorTemplate = buf.toString("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(HttpServerRequest request, Handler<Object> next) {
        // inside middleware the original request has been wrapped with yoke's
        // implementation
        final YokeHttpServerRequest req = (YokeHttpServerRequest) request;

        if (req.response().getStatusCode() < 400) {
            req.response().setStatusCode(getErrorCode(req.get("error")));
        }

        if (req.get("error") == null) {
            req.put("error", req.response().getStatusCode());
        }
        String errorMessage = getMessage(req.get("error"));
        int errorCode = req.response().getStatusCode();
        List<String> stackTrace = getStackTrace(req.get("error"));

        String accept = req.headers().get("accept");

        if (accept == null) {
            accept = "text/plain";
        }

        if (accept.contains("html")) {
            StringBuilder stack = new StringBuilder();
            for (String s : stackTrace) {
                stack.append("<li>");
                stack.append(s);
                stack.append("</li>");
            }

            req.response().putHeader("Content-Type", "text/html");
            req.response().end(
                    errorTemplate.replace("{title}", (String) req.get("title"))
                            .replace("{errorCode}", Integer.toString(errorCode))
                            .replace("{errorMessage}", errorMessage)
                            .replace("{stackTrace}", stack.toString()));
        } else if (accept.contains("json")) {
            JsonObject jsonError = new JsonObject();
            jsonError.putObject("error", new JsonObject().putNumber("code", errorCode).putString("message", errorMessage));
            if (!stackTrace.isEmpty()) {
                JsonArray stack = new JsonArray();
                for (String t : stackTrace) {
                    stack.addString(t);
                }
                jsonError.putArray("stack", stack);
            }
            req.response().putHeader("Content-Type", "application/json");
            req.response().end(jsonError.encode());
        } else {
            req.response().putHeader("Content-Type", "text/plain");

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

            req.response().end(sb.toString());
        }
    }
}
