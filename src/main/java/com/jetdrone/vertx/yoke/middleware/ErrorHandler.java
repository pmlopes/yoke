/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
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
            Buffer buf = vertx.fileSystem().readFileSync(Utils.urlToPath(getClass().getResource("error.html")));
            errorTemplate = buf.toString("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(YokeHttpServerRequest request, Handler<Object> next) {

        YokeHttpServerResponse response = request.response();

        if (response.getStatusCode() < 400) {
            response.setStatusCode(getErrorCode(request.get("error")));
        }

        if (request.get("error") == null) {
            request.put("error", response.getStatusCode());
        }
        String errorMessage = getMessage(request.get("error"));
        int errorCode = response.getStatusCode();
        List<String> stackTrace = getStackTrace(request.get("error"));

        String accept = request.getHeader("accept", "text/plain");

        if (accept.contains("html")) {
            StringBuilder stack = new StringBuilder();
            for (String s : stackTrace) {
                stack.append("<li>");
                stack.append(s);
                stack.append("</li>");
            }

            response.putHeader("Content-Type", "text/html");
            response.end(
                    errorTemplate.replace("{title}", (String) request.get("title"))
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
            response.putHeader("Content-Type", "application/json");
            response.end(jsonError.encode());
        } else {
            response.putHeader("Content-Type", "text/plain");

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
        }
    }
}
