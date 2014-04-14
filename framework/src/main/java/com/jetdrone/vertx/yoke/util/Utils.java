/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.util;

import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.*;
import java.util.Set;

public final class Utils {

    // no instantiation
    private Utils () {}

    private final static char[] HEXARRAY = "0123456789ABCDEF".toCharArray();

    public static String hex(@NotNull byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEXARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEXARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static final String BASE64ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    private static byte[] zeroPad(final int length, final byte[] bytes) {
        final byte[] padded = new byte[length];
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        return padded;
    }

    public static String base64(final @NotNull byte[] stringArray) {

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
    public static Buffer readResourceToBuffer(@NotNull Class<?> clazz, @NotNull String resource) {
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
    public static String readResourceToString(@NotNull Class<?> clazz, @NotNull String resource) {
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
     * Creates a new Message Authentication Code
     * @param algorithm algorithm to use e.g.: HmacSHA256
     * @param secret The secret key used to create signatures
     * @return Mac implementation
     */
    public static Mac newHmac(@NotNull String algorithm, @NotNull String secret) {
        try {
            Mac hmac = Mac.getInstance(algorithm);
            hmac.init(new SecretKeySpec(secret.getBytes(), hmac.getAlgorithm()));
            return hmac;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static Signature newSignature(@NotNull String algorithm) {
        try {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();

            Signature instance = Signature.getInstance(algorithm);
            instance.initSign(privateKey);

            return instance;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new Crypto KEY
     * @param secret The secret key used to create signatures
     * @return Key implementation
     */
    public static Key newCryptoKey(@NotNull String secret) {
        return new SecretKeySpec(secret.getBytes(), "AES");
    }

    /**
     * Signs a String value with a given MAC
     */
    public static String sign(@NotNull String val, @NotNull Mac mac) {
        mac.reset();
        return val + "." + base64(mac.doFinal(val.getBytes()));
    }

    /**
     * Returns the original value is the signature is correct. Null otherwise.
     */
    public static String unsign(@NotNull String val, @NotNull Mac mac) {
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

    public static String encrypt(@NotNull String val, @NotNull Key key) {
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(val.getBytes());
            return base64(encVal);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(@NotNull String val, @NotNull Key key) {
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decordedValue = DatatypeConverter.parseBase64Binary(val);
            byte[] decValue = c.doFinal(decordedValue);
            return new String(decValue);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String escape(@NotNull String html) {
        return html
                .replaceAll("&", "&amp;")
                .replaceAll("\"", "&quot;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }

    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    private static final String XSLT = Utils.readResourceToString(Utils.class, "xml-to-json.xsl");

    public static JsonObject xmlToJson(@NotNull String xml) throws TransformerException {
        // allocate the size of the xml (this is probably is more than needed but avoid re-allocations)
        StringWriter out = new StringWriter(xml.length());

        Transformer transformer = TRANSFORMER_FACTORY.newTransformer(new StreamSource(new StringReader(XSLT)));
        transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(out));

        return new JsonObject(out.toString());
    }

    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

    public static String jsonToXml(@NotNull JsonObject json, @NotNull String rootName) throws XMLStreamException {
        Writer xml = new StringWriter();
        // Create an XML stream writer
        XMLStreamWriter xmlw = XML_OUTPUT_FACTORY.createXMLStreamWriter(xml);

        // Write XML prologue
        xmlw.writeStartDocument();
        // perform real conversion
        jsonToXml(xmlw, json, rootName);
        // Close the writer to flush the output
        xmlw.close();

        return xml.toString();
    }

    private static void jsonToXml(XMLStreamWriter writer, JsonObject json, String name) throws XMLStreamException {
        // get all field names
        Set<String> fields = json.getFieldNames();

        if (fields.size() == 0) {
            // start with element name
            writer.writeEmptyElement(name);
            return;
        }

        // start with element name
        writer.writeStartElement(name);
        for (String field : fields) {
            Object value = json.getField(field);
            if (value != null) {
                if (value instanceof JsonObject) {
                    // process child
                    jsonToXml(writer, (JsonObject) value, field);
                } else if (value instanceof JsonArray) {
                    jsonToXml(writer, (JsonArray) value, field);
                } else {
                    // Writing a few attributes
                    if (field.charAt(0) == '@') {
                        writer.writeAttribute(field.substring(1), json.getValue(field).toString());
                    } else if ("$".equals(field)) {
                        writer.writeCharacters(json.getValue(field).toString());
                    }
                }
            }
        }
        // end with element name
        writer.writeEndElement();
    }

    private static void jsonToXml(XMLStreamWriter writer, JsonArray json, String name) throws XMLStreamException {
        if (json.size() == 0) {
            // start with element name
            writer.writeEmptyElement(name);
            return;
        }

        for (int i = 0 ; i < json.size(); i++) {
            Object element = json.get(i);

            if (element == null) {
                writer.writeEmptyElement(name);
                continue;
            }

            if (element instanceof JsonObject) {
                jsonToXml(writer, (JsonObject) element, name);
                continue;
            }

            if (element instanceof JsonArray) {
                jsonToXml(writer, (JsonArray) element, name);
                continue;
            }

            // start with element name
            writer.writeStartElement(name);
            writer.writeCharacters(element.toString());
            // end with element name
            writer.writeEndElement();
        }
    }

    public static String encodeURIComponent(@NotNull String s) {
        String result;

        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("%21", "!")
                    .replaceAll("%27", "'")
                    .replaceAll("%28", "(")
                    .replaceAll("%29", ")")
                    .replaceAll("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    public static String decodeURIComponent(@NotNull String s) {
        String result;

        try {
            result = URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }
}