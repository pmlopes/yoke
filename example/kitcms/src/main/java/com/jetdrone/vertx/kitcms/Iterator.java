package com.jetdrone.vertx.kitcms;

import org.vertx.java.core.json.JsonArray;

public class Iterator {

    public static abstract class Async {
        public abstract void handle(Object o, Iterator next);
    }

    private final JsonArray array;
    private int index = -1;
    private Async callback;

    public Iterator(JsonArray array) {
        this.array = array;
    }

    public void forEach(Async next) {
        callback = next;
        next();
    }

    public void next() {
        if (++index < array.size()) {
            Object o = array.get(index);
            callback.handle(o, this);
        } else {
            callback.handle(null, null);
        }
    }
}
