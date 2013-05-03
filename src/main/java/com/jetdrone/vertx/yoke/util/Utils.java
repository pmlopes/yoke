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
package com.jetdrone.vertx.yoke.util;

import org.vertx.java.core.buffer.Buffer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public final class Utils {

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

    /**
     * Avoid using this method for constant reads, use it only for one time only reads from resources in the classpath
     */
    public static Buffer readResourceToBuffer(Class<?> clazz, String resource) {
        try {
            Buffer buffer = new Buffer(0);

            try (InputStream in = clazz.getResourceAsStream(resource)) {
                int read;
                byte[] data = new byte[4096];
                while ((read = in.read(data, 0, data.length)) != -1) {
                    if (read == data.length) {
                        buffer.appendBytes(data);
                    } else {
                        byte[] slice = new byte[read];
                        System.arraycopy(data, 0, slice, 0, slice.length);
                        buffer.appendBytes(slice);
                    }
                }
            }

            return buffer;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Creates a new HmacSHA256 Message Authentication Code
     * @param secret The secret key used to create signatures
     * @return Mac implementation
     */
    public static Mac newHmacSHA256(String secret) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            hmacSHA256.init(new SecretKeySpec(secret.getBytes(), hmacSHA256.getAlgorithm()));
            return hmacSHA256;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Signs a String value with a given MAC
     */
    public static String sign(String val, Mac mac) {
        mac.reset();
        return val + "." + base64(mac.doFinal(val.getBytes()));
    }

    /**
     * Returns the original value is the signature is correct. Null otherwise.
     */
    public static String unsign(String val, Mac mac) {
        int idx = val.lastIndexOf('.');

        if (idx == -1) {
            return null;
        }

        String str = val.substring(0, idx);
        if (val.equals(sign(str, mac))) {
            return str;
        }
        return null;
    }
}
