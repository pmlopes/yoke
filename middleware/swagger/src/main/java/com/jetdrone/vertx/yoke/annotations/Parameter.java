package com.jetdrone.vertx.yoke.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface Parameter {

    public static enum Type {
        INTEGER,
        NUMBER,
        STRING,
        BOOLEAN
    }

    public static enum Format {
        INT32,
        INT64,
        FLOAT,
        DOUBLE,
        BYTE,
        DATE,
        DATE_TIME
    }

    public static enum ParamType {
        PATH,
        QUERY,
        BODY,
        FORM,
        HEADER
    }

    String name();
    String description() default "";
    boolean required() default false;
//    Type type();
//    Format format();
    ParamType paramType() default ParamType.PATH;
    boolean allowMultiple() default false;
//    String minimum();
//    String maximum();
}
