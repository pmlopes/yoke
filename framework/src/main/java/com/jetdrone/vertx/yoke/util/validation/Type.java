package com.jetdrone.vertx.yoke.util.validation;

public enum Type {
    Any,
    // base json types
    JsonObject,
    JsonArray,
    String,
    Number,
    Boolean,
    Null,
    // specific types
    Integer,
    Long,
    Double,
    // json schema validations
    DateTime,
    Date,
    Time,
    Email,
    IPAddress,
    IPV6Address,
    URI,
    Hostname,
    Alpha,
    Alphanumeric
}
