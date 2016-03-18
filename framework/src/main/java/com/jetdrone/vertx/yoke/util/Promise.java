package com.jetdrone.vertx.yoke.util;

import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Handler;

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
            if (handler != null) {
                if (handler instanceof AsyncResultHandler) {
                    handler.handle(new YokeAsyncResult<T>(error, null));
                    handler = null;
                    return;
                }
                handler.handle(error);
                handler = null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void resolve(T value) {
        if (!canceled && !resolved) {
            resolved = true;
            if (handler != null) {
                if (handler instanceof AsyncResultHandler) {
                    handler.handle(new YokeAsyncResult<>(value));
                    handler = null;
                    return;
                }
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
