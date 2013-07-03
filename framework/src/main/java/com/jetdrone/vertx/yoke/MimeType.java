/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetdrone.vertx.yoke;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic MimeType support inspired by the Apache Http Server project.
 */
public class MimeType {

    private static final Map<String, String> mimes = new HashMap<>();
    private static final String defaultContentEncoding = Charset.defaultCharset().name();

    private static void loadFile(InputStream in) {

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

    static {
        loadFile(MimeType.class.getResourceAsStream("mime.types"));
        loadFile(MimeType.class.getResourceAsStream("mimex.types"));
    }

    /**
     * Returns a mime type string by parsing the file extension of a file string. If the extension is not found or
     * unknown the default value is returned.
     *
     * @param file - path to a file with extension
     * @param defaultMimeType - what to return if not found
     * @return mime type string
     */
    public static String getMime(String file, String defaultMimeType) {
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
     * @see MimeType#getMime(String, String)
     * @param file - path to a file with extension
     * @return mime type string
     */
    public static String getMime(String file) {
        return getMime(file, "text/plain");
    }

    /**
     * Gets the default charset for a file.
     * for now all mime types that start with text returns UTF-8 otherwise the fallback.
     *
     * @param mime the mime type to query
     * @param fallback if not found returns fallback
     * @return charset string
     */
    public static String getCharset(String mime, String fallback) {
        // TODO: exceptions json and which other should also be marked as text
        if (mime.startsWith("text")) {
            return defaultContentEncoding;
        }

        return fallback;
    }

    /**
     * Gets the default charset for a file with default fallback null
     *
     * @see MimeType#getCharset(String, String)
     * @param mime the mime type to query
     * @return charset string
     */
    public static String getCharset(String mime) {
        return getCharset(mime, null);
    }
}
