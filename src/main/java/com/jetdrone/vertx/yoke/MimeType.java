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
import java.util.HashMap;
import java.util.Map;

public class MimeType {

    private static final Map<String, String> mimes = new HashMap<>();

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

    public static String getMime(String file) {
        return getMime(file, "text/plain");
    }

    public static String getCharset(String mime, String fallback) {
        if (mime.startsWith("text")) {
            return "UTF-8";
        }

        return fallback;
    }

    public static String getCharset(String mime) {
        return getCharset(mime, null);
    }
}
