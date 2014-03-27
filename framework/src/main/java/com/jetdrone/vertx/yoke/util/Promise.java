package com.jetdrone.vertx.yoke.util;

import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;

final class Promise<T> {

    @SuppressWarnings("unchecked")
    private Handler handler;

    private boolean canceled;
    private boolean resolved;

    public void then(Handler<T> handler) {
        this.handler = handler;
    }

    public void then(AsyncResultHandler<T> handler) {
        this.handler = handler;
    }

    @SuppressWarnings("unchecked")
    public void cancel(Object error) {
        if (!canceled) {
            canceled = true;
            if (handler instanceof AsyncResultHandler) {
                handler.handle(new YokeAsyncResult<T>(error, null));
                handler = null;
                return;
            }
            if (handler != null) {
                handler.handle(null);
                handler = null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void resolve(T value) {
        if (!canceled && !resolved) {
            resolved = true;
            if (handler instanceof AsyncResultHandler) {
                handler.handle(new YokeAsyncResult<>(value));
                handler = null;
                return;
            }
            if (handler != null) {
                handler.handle(value);
                handler = null;
            }
        }
    }

    public boolean isCanceled() {
        return canceled;
    }

    public boolean isResolved() {
        return resolved;
    }
}
