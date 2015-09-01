/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.util;

import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Set;

public final class Utils {

    // no instantiation
    private Utils () {}

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
     * Avoid using this method for constant reads, use it only for one time only reads from resources in the classpath
     */
    public static String readFileToString(@NotNull String resource) {
        try {
            try (Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(resource), "UTF-8"))) {

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
     * Avoid using this method for constant reads, use it only for one time only reads from resources in the classpath
     */
    public static String readURLToString(@NotNull String resource) {
        try {
            try (Reader r = new BufferedReader(new InputStreamReader(new URL(resource).openStream(), "UTF-8"))) {

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