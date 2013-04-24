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
package com.jetdrone.vertx.yoke.middleware;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

final class Utils {

    // no instantiation
    private Utils () {}

    private static final String BASE64ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    private static byte[] zeroPad(final int length, final byte[] bytes) {
        final byte[] padded = new byte[length];
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        return padded;
    }

    public static String base64(final byte[] stringArray) {

        final StringBuilder encoded = new StringBuilder();

        // determine how many padding bytes to add to the output
        final int paddingCount = (3 - (stringArray.length % 3)) % 3;
        // add any necessary padding to the input
        final byte[] paddedArray = zeroPad(stringArray.length + paddingCount, stringArray);
        // process 3 bytes at a time, churning out 4 output bytes
        for (int i = 0; i < paddedArray.length; i += 3) {
            final int j = ((paddedArray[i] & 0xff) << 16) +
                    ((paddedArray[i + 1] & 0xff) << 8) +
                    (paddedArray[i + 2] & 0xff);

            encoded.append(BASE64ALPHA.charAt((j >> 18) & 0x3f));
            encoded.append(BASE64ALPHA.charAt((j >> 12) & 0x3f));
            encoded.append(BASE64ALPHA.charAt((j >> 6) & 0x3f));
            encoded.append(BASE64ALPHA.charAt(j & 0x3f));
        }

        encoded.setLength(encoded.length() - paddingCount);
        return encoded.toString();
    }

    public static String urlToPath(URL url) {
        try {
            // something like /c:/foo%20bar/baz.jpg
            String path = URLDecoder.decode(url.getPath(), "UTF-8");
            return new File(path).getPath();
        } catch (UnsupportedEncodingException ueex) {
            throw new RuntimeException(ueex);
        }
    }
}
