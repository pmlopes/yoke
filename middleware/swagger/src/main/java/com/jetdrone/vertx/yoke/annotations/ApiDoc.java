package com.jetdrone.vertx.yoke.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ApiDoc {
    String summary();
//    String type();
    String[] notes() default {};
//    Object authorizations();
    Parameter[] parameters() default {};
    ResponseMessage[] responseMessages() default {};
}
