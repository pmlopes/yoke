package com.jetdrone.vertx.yoke.util;

import java.util.Date;
import java.util.regex.Pattern;

class Validator {

    public static String toString(Object input) {
        throw new UnsupportedOperationException();
    }

    public static Date toDate(Object input) {
        throw new UnsupportedOperationException();
    }

    public static Float toFloat(Object input) {
        throw new UnsupportedOperationException();
    }

    public static Boolean toBoolean(Object input, boolean strict) {
        throw new UnsupportedOperationException();
    }

    public static Boolean toBoolean(Object input) {
        return toBoolean(input, true);
    }

    public static Object flatten(Object array, Object separator) {
        throw new UnsupportedOperationException();
    }

    public static Object merge(Object obj, Object defaults) {
        throw new UnsupportedOperationException();
    }

    public static boolean equals(String str, Object comparison) {
        throw new UnsupportedOperationException();
    }

    public static boolean contains(String str, Object element) {
        throw new UnsupportedOperationException();
    }

    public static boolean matches(String str, Pattern pattern) {
        throw new UnsupportedOperationException();
    }

    public static boolean isEmail(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean isURL(String str, Object options) {
        throw new UnsupportedOperationException();
    }

    public static boolean isIP(String str, int version) {
        throw new UnsupportedOperationException();
    }

    public static boolean isIP(String str) {
        return isIP(str, 4) || isIP(str, 6);
    }

    public static boolean isAlpha(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean isAlphanumeric(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean isNumeric(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean isHexadecimal(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean isHexColor(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean isLowercase(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean isUppercase(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean isInt(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean isFloat(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean isDivisibleBy(String str, Number num) {
        throw new UnsupportedOperationException();
    }

    public static boolean isNull(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean isLength(String str, int min, int max) {
        throw new UnsupportedOperationException();
    }

    public static boolean isLength(String str, int min) {
        throw new UnsupportedOperationException();
    }

    public static boolean isUUID(String str, int version) {
        throw new UnsupportedOperationException();
    }

    public static boolean isUUID(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean isDate(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean isAfter(String str, Date date) {
        throw new UnsupportedOperationException();
    }

    public static boolean isBefore(String str, Date date) {
        throw new UnsupportedOperationException();
    }

    public static String ltrim(String str, String chars) {
        throw new UnsupportedOperationException();
    }

    public static String rtrim(String str, String chars) {
        throw new UnsupportedOperationException();
    }

    public static String trim(String str, String chars) {
        throw new UnsupportedOperationException();
    }

    public static String escape(String str) {
        throw new UnsupportedOperationException();
    }

    public static String whitelist(String str, String chars) {
        throw new UnsupportedOperationException();
    }

    public static String blacklist(String str, String chars) {
        throw new UnsupportedOperationException();
    }
}
