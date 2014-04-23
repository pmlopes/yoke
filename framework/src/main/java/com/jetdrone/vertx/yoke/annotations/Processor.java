package com.jetdrone.vertx.yoke.annotations;

import java.lang.annotation.Annotation;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class Processor {

    private static final MethodHandles.Lookup lookup = MethodHandles.publicLookup();

    private Processor() {}

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

    public static <T extends Annotation> T getAnnotation(Class c, Class<T> annotation) {
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
