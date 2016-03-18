package com.jetdrone.vertx.yoke.annotations.processors;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.annotations.*;
import com.jetdrone.vertx.yoke.middleware.Router;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ContentNegotiationProcessorHandler extends AbstractAnnotationHandler<Router> {

    public ContentNegotiationProcessorHandler() {
        super(Router.class);
    }

    @Override
    public void process(final Router router, final Object instance, final Class<?> clazz, final Method method) {

        Produces clazzProducesAnn = Processor.getAnnotation(clazz, Produces.class);
        Consumes clazzConsumesAnn = Processor.getAnnotation(clazz, Consumes.class);

        String[] clazzProduces = clazzProducesAnn != null ? clazzProducesAnn.value() : null;
        String[] clazzConsumes = clazzConsumesAnn != null ? clazzConsumesAnn.value() : null;

        Produces producesAnn = Processor.getAnnotation(method, Produces.class);
        Consumes consumesAnn = Processor.getAnnotation(method, Consumes.class);

        String[] produces = producesAnn != null ? producesAnn.value() : null;
        String[] consumes = consumesAnn != null ? consumesAnn.value() : null;

        if (produces == null) {
            produces = clazzProduces;
        }
        if (consumes == null) {
            consumes = clazzConsumes;
        }

        if (produces == null && consumes == null) {
            return;
        }

        // process the methods that have both YokeRequest and Handler

        if (Processor.isCompatible(method, ALL.class, YokeRequest.class, Handler.class)) {
            router.all(Processor.getAnnotation(method, ALL.class).value(), wrap(consumes, produces));
        }
        if (Processor.isCompatible(method, CONNECT.class, YokeRequest.class, Handler.class)) {
            router.connect(Processor.getAnnotation(method, CONNECT.class).value(), wrap(consumes, produces));
        }
        if (Processor.isCompatible(method, OPTIONS.class, YokeRequest.class, Handler.class)) {
            router.options(Processor.getAnnotation(method, OPTIONS.class).value(), wrap(consumes, produces));
        }
        if (Processor.isCompatible(method, HEAD.class, YokeRequest.class, Handler.class)) {
            router.head(Processor.getAnnotation(method, HEAD.class).value(), wrap(consumes, produces));
        }
        if (Processor.isCompatible(method, GET.class, YokeRequest.class, Handler.class)) {
            router.get(Processor.getAnnotation(method, GET.class).value(), wrap(consumes, produces));
        }
        if (Processor.isCompatible(method, POST.class, YokeRequest.class, Handler.class)) {
            router.post(Processor.getAnnotation(method, POST.class).value(), wrap(consumes, produces));
        }
        if (Processor.isCompatible(method, PUT.class, YokeRequest.class, Handler.class)) {
            router.put(Processor.getAnnotation(method, PUT.class).value(), wrap(consumes, produces));
        }
        if (Processor.isCompatible(method, PATCH.class, YokeRequest.class, Handler.class)) {
            router.patch(Processor.getAnnotation(method, PATCH.class).value(), wrap(consumes, produces));
        }
        if (Processor.isCompatible(method, DELETE.class, YokeRequest.class, Handler.class)) {
            router.delete(Processor.getAnnotation(method, DELETE.class).value(), wrap(consumes, produces));
        }
    }

    @Override
    public void process(Router router, Object instance, Class<?> clazz, Field field) {
        // NOOP
    }

    private static Middleware wrap(final String[] consumes, final String[] produces) {
        return new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                // we only know how to process certain media types
                if (consumes != null) {
                    boolean canConsume = false;
                    for (String c : consumes) {
                        if (request.is(c)) {
                            canConsume = true;
                            break;
                        }
                    }

                    if (!canConsume) {
                        // 415 Unsupported Media Type (we don't know how to handle this media)
                        next.handle(415);
                        return;
                    }
                }

                // the object was marked with a specific content type
                if (produces != null) {
                    String bestContentType = request.accepts(produces);

                    // the client does not know how to handle our content type, return 406
                    if (bestContentType == null) {
                        next.handle(406);
                        return;
                    }

                    // mark the response with the correct content type (which allows middleware to know it later on)
                    request.response().setContentType(bestContentType);
                }

                // the request can be handled, it does respect the content negotiation
                next.handle(null);
            }
        };
    }
}
