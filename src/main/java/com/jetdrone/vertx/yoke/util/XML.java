package com.jetdrone.vertx.yoke.util;

import org.vertx.java.core.json.JsonObject;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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

    // Namespaces
    private static final String GARDENING = "http://com.bdaum.gardening";
    private static final String XHTML = "http://www.w3.org/1999/xhtml";

    public static void main(String[] args) throws XMLStreamException {


        // Create an output factory
        XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        // Set namespace prefix defaulting for all created writers
        xmlof.setProperty("javax.xml.stream.isPrefixDefaulting",Boolean.TRUE);

        // Create an XML stream writer
        XMLStreamWriter xmlw =
                xmlof.createXMLStreamWriter(System.out);

        // Write XML prologue
        xmlw.writeStartDocument();
        // Write a processing instruction
        xmlw.writeProcessingInstruction(
                "xml-stylesheet href='catalog.xsl' type='text/xsl'");
        // Now start with root element
        xmlw.writeStartElement("product");
        // Set the namespace definitions to the root element
        // Declare the default namespace in the root element
        xmlw.writeDefaultNamespace(GARDENING);
        // Writing a few attributes
        xmlw.writeAttribute("productNumber","3923-1");
        xmlw.writeAttribute("name","Nightshadow");
        // Declare XHTML prefix
//    xmlw.setPrefix("xhtml",XHTML);
        // Different namespace for description element
        xmlw.writeStartElement(XHTML,"description");
        // Declare XHTML namespace in the scope of the description element
//    xmlw.writeNamespace("xhtml",XHTML);
        xmlw.writeCharacters(
                "A tulip of almost black color. \nBlossoms in April & May");
        xmlw.writeEndElement();
        // Shorthand for empty elements
        xmlw.writeEmptyElement("supplier");
        xmlw.writeAttribute("name","Floral22");
//    xmlw.writeEndElement();
        // Write document end. This closes all open structures
        xmlw.writeEndDocument();
        // Close the writer to flush the output
        xmlw.close();
    }

}
