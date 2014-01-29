/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.core.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * # ThreadLocalUTCDateFormat
 *
 */
public final class ThreadLocalUTCDateFormat extends ThreadLocal<DateFormat> {

    public final String format(Date date) {
        return get().format(date);
    }

    public String format(Object value) {
        return get().format(value);
    }

    public final Date parse(String text) throws ParseException {
        return get().parse(text);
    }

    @Override
    protected DateFormat initialValue() {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        return df;
    }
}
