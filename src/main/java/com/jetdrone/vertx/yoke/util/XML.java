package com.jetdrone.vertx.yoke.util;

import org.vertx.java.core.json.JsonObject;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class XML {

    private static final TransformerFactory FACTORY = TransformerFactory.newInstance();
    private static final String XSLT = Utils.readResourceToBuffer(XML.class, "xml-to-json.xsl").toString();

    public static JsonObject toJson(String xml) throws TransformerException {
        StringWriter out = new StringWriter();

        Transformer transformer = FACTORY.newTransformer(new StreamSource(new StringReader(XSLT)));
        transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(out));

        return new JsonObject(out.toString());
    }
}
