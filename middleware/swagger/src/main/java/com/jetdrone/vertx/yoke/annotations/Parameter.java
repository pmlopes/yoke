package com.jetdrone.vertx.yoke.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface Parameter {
    String name();
    String description() default "";
    boolean required() default false;
    DataType type();
    ParamType paramType() default ParamType.PATH;
    boolean allowMultiple() default false;
    String minimum() default "";
    String maximum() default "";
    String[] enumeration() default {};
    // specify the $ref name when type is REF
    String refId() default "";
}
