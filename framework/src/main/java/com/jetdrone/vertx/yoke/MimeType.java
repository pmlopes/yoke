/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * # MimeType
 *
 * Basic MimeType support inspired by the Apache Http Server project.
 */
public class MimeType {

    // This is a singleton
    private MimeType() {}

    /**
     * Internal map with all known mime types
     */
    private static final Map<String, String> mimes = new HashMap<>();

    /**
     * Internal default content encoding (charset)
     */
    private static final String defaultContentEncoding = Charset.defaultCharset().name();

    /**
     * Loads a file from a input stream containing all known mime types. The InputStream is a resource mapped from the
     * project resource directory.
     *
     * @param in InputStream
     */
    private static void loadFile(@NotNull InputStream in) {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String l;

            while ((l = br.readLine()) != null) {
                if (l.length() > 0 && l.charAt(0) != '#') {
                    String[] tokens = l.split("\\s+");
                    for (int i = 1; i < tokens.length; i++) {
                        mimes.put(tokens[i], tokens[0]);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** @constructor MimeType
     * Static constructor to load the mime types from the resource directory inside the jar file.
     */
    static {
        loadFile(MimeType.class.getResourceAsStream("mime.types"));
        loadFile(MimeType.class.getResourceAsStream("mimex.types"));
    }

    /**
     * Returns a mime type string by parsing the file extension of a file string. If the extension is not found or
     * unknown the default value is returned.
     *
     * @param file            path to a file with extension
     * @param defaultMimeType what to return if not found
     * @return mime type
     */
    public static String getMime(@NotNull String file, String defaultMimeType) {
        int sep = file.lastIndexOf('.');
        if (sep != -1) {
            String extension = file.substring(sep + 1, file.length());

            String mime = mimes.get(extension);

            if (mime != null) {
                return mime;
            }
        }

        return defaultMimeType;
    }

    /**
     * Gets the mime type string for a file with fallback to text/plain
     *
     * @param file path to a file with extension
     * @return mime type
     */
    public static String getMime(@NotNull String file) {
        return getMime(file, "text/plain");
    }

    /**
     * Gets the default charset for a file.
     * for now all mime types that start with text returns UTF-8 otherwise the fallback.
     *
     * @param mime     the mime type to query
     * @param fallback if not found returns fallback
     * @return charset string
     */
    public static String getCharset(@NotNull String mime, String fallback) {
        // TODO: exceptions json and which other should also be marked as text
        if (mime.startsWith("text")) {
            return defaultContentEncoding;
        }

        return fallback;
    }

    /**
     * Gets the default charset for a file with default fallback null
     *
     * @param mime the mime type to query
     * @return charset string
     */
    public static String getCharset(@NotNull String mime) {
        return getCharset(mime, null);
    }
}
