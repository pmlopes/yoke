package com.jetdrone.vertx.yoke.annotations.processors;

import com.jetdrone.vertx.yoke.annotations.Processor;
import com.jetdrone.vertx.yoke.annotations.RegExParam;
import com.jetdrone.vertx.yoke.middleware.Router;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

public class RegExParamProcessorHandler extends AbstractAnnotationHandler<Router> {

    public RegExParamProcessorHandler() {
        super(Router.class);
    }

    @Override
    public void process(Router router, Object instance, Class<?> clazz, Method method) {
        // NOOP
    }

    @Override
    public void process(Router router, Object instance, Class<?> clazz, Field field) {
        boolean staticField = false;

        if (Modifier.isStatic(field.getModifiers())) {
            staticField = true;
        }

        RegExParam regExParam = Processor.getAnnotation(field, RegExParam.class);

        if (Processor.isCompatible(field, RegExParam.class, Pattern.class)) {
            try {
                router.param(regExParam.value(), (Pattern) field.get(staticField ? null : instance));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        if (Processor.isCompatible(field, RegExParam.class, String.class)) {
            try {
                router.param(regExParam.value(), Pattern.compile((String) field.get(staticField ? null : instance)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
