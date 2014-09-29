package com.jetdrone.vertx.yoke.annotations;

import com.jetdrone.vertx.yoke.annotations.processors.ContentNegotiationProcessorHandler;
import com.jetdrone.vertx.yoke.annotations.processors.JsonSchemaProcessorHandler;
import com.jetdrone.vertx.yoke.annotations.processors.RegExParamProcessorHandler;
import com.jetdrone.vertx.yoke.annotations.processors.RouterProcessorHandler;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class Processor {

    private static final MethodHandles.Lookup lookup = MethodHandles.publicLookup();

    private static final List<AnnotationHandler<?>> handlers = new ArrayList<>();

    private Processor() {
    }

    static {
        handlers.add(new ContentNegotiationProcessorHandler());
        handlers.add(new JsonSchemaProcessorHandler());
        handlers.add(new RegExParamProcessorHandler());
        handlers.add(new RouterProcessorHandler());
    }

    public static void registerProcessor(String className) {
        try {
            registerProcessor(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void registerProcessor(Class<?> processor) {
        try {
            // if already registered skip
            for (AnnotationHandler<?> annotationHandler : handlers) {
                if (annotationHandler.getClass().equals(processor)) {
                    // skip
                    return;
                }
            }

            if (AnnotationHandler.class.isAssignableFrom(processor)) {
                // always insert before router processor
                handlers.add(handlers.size() - 1, (AnnotationHandler) processor.newInstance());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void process(@NotNull T context, @NotNull Object instance) {
        final Class<?> clazz = instance.getClass();

        for (final Field field : clazz.getFields()) {
            for (AnnotationHandler<?> handler : handlers) {
                if (handler.isFor(context.getClass())) {
                    final AnnotationHandler<T> _handler = (AnnotationHandler<T>) handler;
                    _handler.process(context, instance, clazz, field);
                }
            }
        }

        for (final Method method : clazz.getMethods()) {
            for (AnnotationHandler<?> handler : handlers) {
                if (handler.isFor(context.getClass())) {
                    final AnnotationHandler<T> _handler = (AnnotationHandler<T>) handler;
                    _handler.process(context, instance, clazz, method);
                }
            }
        }
    }

    public static MethodHandle getMethodHandle(Method m, Class<?>... paramTypes) {
        try {
            Class[] methodParamTypes = m.getParameterTypes();

            if (methodParamTypes != null) {
                if (methodParamTypes.length == paramTypes.length) {
                    for (int i = 0; i < methodParamTypes.length; i++) {
                        if (!paramTypes[i].isAssignableFrom(methodParamTypes[i])) {
                            // for groovy and other languages that do not do type check at compile time
                            if (!methodParamTypes[i].equals(Object.class)) {
                                return null;
                            }
                        }
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }

            MethodHandle methodHandle = lookup.unreflect(m);
            CallSite callSite = new ConstantCallSite(methodHandle);
            return callSite.dynamicInvoker();

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isCompatible(Method m, Class<? extends Annotation> annotation, Class<?>... paramTypes) {
        if (getAnnotation(m, annotation) != null) {
            if (getMethodHandle(m, paramTypes) != null) {
                return true;
            } else {
                throw new RuntimeException("Method signature not compatible!");
            }
        }
        return false;
    }

    public static boolean isCompatible(Field f, Class<? extends Annotation> annotation, Class<?> type) {
        if (getAnnotation(f, annotation) != null) {
            Class<?> fieldType = f.getType();

            if (fieldType != null) {
                if (!type.isAssignableFrom(fieldType)) {
                    return false;
                }
            } else {
                return false;
            }

            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotation(Method m, Class<T> annotation) {
        // skip static methods
        if (Modifier.isStatic(m.getModifiers())) {
            return null;
        }
        // skip non public methods
        if (!Modifier.isPublic(m.getModifiers())) {
            return null;
        }

        Annotation[] annotations = m.getAnnotations();
        // this method is not annotated
        if (annotations == null) {
            return null;
        }

        // verify if the method is annotated
        for (Annotation ann : annotations) {
            if (ann.annotationType().equals(annotation)) {
                return (T) ann;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotation(Field f, Class<T> annotation) {
        // skip non final methods
        if (!Modifier.isFinal(f.getModifiers())) {
            return null;
        }
        // skip non public methods
        if (!Modifier.isPublic(f.getModifiers())) {
            return null;
        }

        Annotation[] annotations = f.getAnnotations();
        // this method is not annotated
        if (annotations == null) {
            return null;
        }

        // verify if the method is annotated
        for (Annotation ann : annotations) {
            if (ann.annotationType().equals(annotation)) {
                return (T) ann;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotation(Class<?> c, Class<T> annotation) {
        // skip non public classes
        if (!Modifier.isPublic(c.getModifiers())) {
            return null;
        }

        Annotation[] annotations = c.getAnnotations();
        // this method is not annotated
        if (annotations == null) {
            return null;
        }

        // verify if the method is annotated
        for (Annotation ann : annotations) {
            if (ann.annotationType().equals(annotation)) {
                return (T) ann;
            }
        }

        return null;
    }
}
