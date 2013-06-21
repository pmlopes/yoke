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
import org.vertx.java.core.json.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
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
     * Avoid using this method for constant reads, use it only for one time only reads from resources in the classpath
     */
    public static String readResourceToString(Class<?> clazz, String resource) {
        try {
            try (Reader r = new BufferedReader(new InputStreamReader(clazz.getResourceAsStream(resource), "UTF-8"))) {

                Writer writer = new StringWriter();

                char[] buffer = new char[1024];
                int n;
                while ((n = r.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

                return writer.toString();
            }
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

    public static String escape(String html) {
        return html
                .replaceAll("&", "&amp;")
                .replaceAll("\"", "&quot;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }

    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    private static final String XSLT = Utils.readResourceToString(XML.class, "xml-to-json.xsl");

    public static JsonObject XMLToJson(String xml) throws TransformerException {
        // allocate the size of the xml (this is probably is more than needed but avoid re-allocations)
        StringWriter out = new StringWriter(xml.length());

        Transformer transformer = TRANSFORMER_FACTORY.newTransformer(new StreamSource(new StringReader(XSLT)));
        transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(out));

        return new JsonObject(out.toString());
    }

    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

    public static String JsonToXML(JsonObject json) throws XMLStreamException {
        Writer xml = new StringWriter();
        // Create an XML stream writer
        XMLStreamWriter xmlw = XML_OUTPUT_FACTORY.createXMLStreamWriter(xml);

        // Write XML prologue
        xmlw.writeStartDocument();

        // TODO: real conversion

        // Write document end. This closes all open structures
        xmlw.writeEndDocument();
        // Close the writer to flush the output
        xmlw.close();

        return xml.toString();
    }
}
