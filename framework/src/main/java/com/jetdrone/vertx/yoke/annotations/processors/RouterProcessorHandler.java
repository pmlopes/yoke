package com.jetdrone.vertx.yoke.annotations.processors;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.annotations.*;
import com.jetdrone.vertx.yoke.middleware.Router;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RouterProcessorHandler extends AbstractAnnotationHandler<Router> {

    public RouterProcessorHandler() {
        super(Router.class);
    }

    @Override
    public void process(final Router router, final Object instance, final Class<?> clazz, final Method method) {

        // process the methods that have both YokeRequest and Handler

        if (Processor.isCompatible(method, ALL.class, YokeRequest.class, Handler.class)) {
            MethodHandle methodHandle = Processor.getMethodHandle(method, YokeRequest.class, Handler.class);
            router.all(Processor.getAnnotation(method, ALL.class).value(), wrap(instance, methodHandle));
        }
        if (Processor.isCompatible(method, CONNECT.class, YokeRequest.class, Handler.class)) {
            MethodHandle methodHandle = Processor.getMethodHandle(method, YokeRequest.class, Handler.class);
            router.connect(Processor.getAnnotation(method, CONNECT.class).value(), wrap(instance, methodHandle));
        }
        if (Processor.isCompatible(method, OPTIONS.class, YokeRequest.class, Handler.class)) {
            MethodHandle methodHandle = Processor.getMethodHandle(method, YokeRequest.class, Handler.class);
            router.options(Processor.getAnnotation(method, OPTIONS.class).value(), wrap(instance, methodHandle));
        }
        if (Processor.isCompatible(method, HEAD.class, YokeRequest.class, Handler.class)) {
            MethodHandle methodHandle = Processor.getMethodHandle(method, YokeRequest.class, Handler.class);
            router.head(Processor.getAnnotation(method, HEAD.class).value(), wrap(instance, methodHandle));
        }
        if (Processor.isCompatible(method, GET.class, YokeRequest.class, Handler.class)) {
            MethodHandle methodHandle = Processor.getMethodHandle(method, YokeRequest.class, Handler.class);
            router.get(Processor.getAnnotation(method, GET.class).value(), wrap(instance, methodHandle));
        }
        if (Processor.isCompatible(method, POST.class, YokeRequest.class, Handler.class)) {
            MethodHandle methodHandle = Processor.getMethodHandle(method, YokeRequest.class, Handler.class);
            router.post(Processor.getAnnotation(method, POST.class).value(), wrap(instance, methodHandle));
        }
        if (Processor.isCompatible(method, PUT.class, YokeRequest.class, Handler.class)) {
            MethodHandle methodHandle = Processor.getMethodHandle(method, YokeRequest.class, Handler.class);
            router.put(Processor.getAnnotation(method, PUT.class).value(), wrap(instance, methodHandle));
        }
        if (Processor.isCompatible(method, PATCH.class, YokeRequest.class, Handler.class)) {
            MethodHandle methodHandle = Processor.getMethodHandle(method, YokeRequest.class, Handler.class);
            router.patch(Processor.getAnnotation(method, PATCH.class).value(), wrap(instance, methodHandle));
        }
        if (Processor.isCompatible(method, DELETE.class, YokeRequest.class, Handler.class)) {
            MethodHandle methodHandle = Processor.getMethodHandle(method, YokeRequest.class, Handler.class);
            router.delete(Processor.getAnnotation(method, DELETE.class).value(), wrap(instance, methodHandle));
        }

        if (Processor.isCompatible(method, Param.class, YokeRequest.class, Handler.class)) {
            MethodHandle methodHandle = Processor.getMethodHandle(method, YokeRequest.class, Handler.class);
            router.param(Processor.getAnnotation(method, Param.class).value(), wrap(instance, methodHandle));
        }
    }

    @Override
    public void process(Router router, Object instance, Class<?> clazz, Field field) {
        // NOOP
    }

    private static Middleware wrap(final Object instance, final MethodHandle m) {
        return new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                try {
                    m.invoke(instance, request, next);
                } catch (Throwable e) {
                    next.handle(e);
                }
            }
        };
    }
}
