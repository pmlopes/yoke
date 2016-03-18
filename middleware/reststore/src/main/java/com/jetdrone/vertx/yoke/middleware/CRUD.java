package com.jetdrone.vertx.yoke.middleware;

import org.jetbrains.annotations.NotNull;
import io.vertx.core.json.JsonObject;

public class CRUD {

    public static interface Handler {
        void handle(@NotNull YokeRequest request, @NotNull final JsonObject filter, @NotNull final io.vertx.core.Handler<JsonObject> next);
    }

    Handler createHandler;
    Handler readHandler;
    Handler updateHandler;
    Handler deleteHandler;


    public CRUD createHandler(Handler handler) {
        this.createHandler = handler;
        return this;
    }

    public CRUD readHandler(Handler handler) {
        this.readHandler = handler;
        return this;
    }

    public CRUD updateHandler(Handler handler) {
        this.updateHandler = handler;
        return this;
    }

    public CRUD deleteHandler(Handler handler) {
        this.deleteHandler = handler;
        return this;
    }
}
