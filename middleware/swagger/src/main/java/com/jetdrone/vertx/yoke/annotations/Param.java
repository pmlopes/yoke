package com.jetdrone.vertx.yoke.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface Param {
    String name();
    String description() default "";
    boolean required() default false;
//    String type();
//    String format();
//    String paramType();
//    boolean allowMultiple();
//    String minimum();
//    String maximum();
}
