package com.jetdrone.vertx.yoke.annotations;

public enum DataType {
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
    REF(null, null, 0),
    UNDEFINED(null, null, 0);

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
        if (type == null) {
            throw new RuntimeException("UNDEFINED cannot be used as a type");
        }
        return type;
    }

    public String format() {
        if (type == null) {
            throw new RuntimeException("UNDEFINED cannot be used as a type");
        }
        return format;
    }

    public boolean isIN() {
        if (type == null) {
            throw new RuntimeException("UNDEFINED cannot be used as a type");
        }
        return (inOut & IN) == IN;
    }

    public boolean isOUT() {
        if (type == null) {
            throw new RuntimeException("UNDEFINED cannot be used as a type");
        }
        return (inOut & OUT) == OUT;
    }
}