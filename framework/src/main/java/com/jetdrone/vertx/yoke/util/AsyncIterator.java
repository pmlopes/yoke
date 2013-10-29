package com.jetdrone.vertx.yoke.util;

import org.vertx.java.core.Handler;

public abstract class AsyncIterator<T> implements Handler<T> {

    private final java.util.Iterator<T> iterator;
    private boolean end = false;

    public AsyncIterator(Iterable<T> iterable) {
        iterator = iterable.iterator();
        next();
    }

    public final boolean isEnd() {
        return end;
    }

    public final void next() {
        if (iterator.hasNext()) {
            handle(iterator.next());
        } else {
            end = true;
            handle(null);
        }
    }

    public final void remove() {
        iterator.remove();
    }
}
