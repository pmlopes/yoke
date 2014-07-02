package com.jetdrone.vertx.yoke.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface Parameter {

    public static enum DataType {
        // primitives
        INTEGER("integer", "int32", DataType.IN + DataType.OUT),
        LONG("integer", "int64", DataType.IN + DataType.OUT),
        FLOAT("number", "float", DataType.IN + DataType.OUT),
        DOUBLE("number", "double", DataType.IN + DataType.OUT),
        STRING("string", null, DataType.IN + DataType.OUT),
        BYTE("string", "byte", DataType.IN + DataType.OUT),
        BOOLEAN("boolean", null, DataType.IN + DataType.OUT),
        DATE("string", "date", DataType.IN + DataType.OUT),
        DATETIME("string", "date-time", DataType.IN + DataType.OUT),
        // containers
        ARRAY("array", null, DataType.IN + DataType.OUT),
        SET("array", null, DataType.IN + DataType.OUT),
        // void
        VOID("void", null, DataType.OUT),
        // file
        FILE("File", null, DataType.IN),
        REF(null, null, 0);

        private static final int IN = 1;
        private static final int OUT = 2;

        private final String type;
        private final String format;
        private final int inOut;

        DataType(String type, String format, int inOut) {
            this.type = type;
            this.format = format;
            this.inOut = inOut;
        }

        public String type() {
            return type;
        }

        public String format() {
            return format;
        }

        public boolean isIN() {
            return (inOut & IN) == IN;
        }

        public boolean isOUT() {
            return (inOut & OUT) == OUT;
        }
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
    DataType type();
    ParamType paramType() default ParamType.PATH;
    boolean allowMultiple() default false;
    String minimum() default "";
    String maximum() default "";
}
