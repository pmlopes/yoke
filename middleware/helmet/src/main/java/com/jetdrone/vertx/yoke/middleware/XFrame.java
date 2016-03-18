package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;

public final class XFrame extends Middleware {

    public static enum Action {
        DENY("DENY"),
        ALLOWFROM("ALLOW-FROM"),
        SAMEORIGIN("SAMEORIGIN");

        private final String action;

        Action(String action) {
            this.action = action;
        }

        @Override
        public String toString() {
            return action;
        }
    }

    private final String header;

    public XFrame() {
        this(Action.DENY);
    }

    public XFrame(Action action) {
        this(action, null);
    }

    public XFrame(Action action, String option) {
        if (action == Action.ALLOWFROM && option == null) {
            throw new RuntimeException("ALLOW-FROM requires a second argument");
        }

        if (action == Action.ALLOWFROM) {
            header = Action.ALLOWFROM.toString() + " " + option;
        } else {
            header = action.toString();
        }
    }

    @Override
    public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        request.response().putHeader("X-FRAME-OPTIONS", header);
        next.handle(null);
    }
}
