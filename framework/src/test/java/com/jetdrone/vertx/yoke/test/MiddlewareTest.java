package com.jetdrone.vertx.yoke.test;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.util.Utils;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import static org.vertx.testtools.VertxAssert.*;

public class MiddlewareTest extends TestVerticle {

    @Test
    public void testMiddleware() {
        final YokeTester yoke = new YokeTester(this);
        yoke.use(new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                assertNotNull(this.vertx);
                testComplete();
            }
        });

        yoke.request("GET", "/", null);
    }

    @Test
    public void testXml() throws TransformerException, XMLStreamException {
        String message = "\n" +
                "\n" +
                "<Customers>\n" +
                "    <Customer Id=\"99\">\n" +
                "        <Name>Bob</Name>\n" +
                "        <Age>39</Age>\n" +
                "        <Address>\n" +
                "            <Street>10 Idle Lane</Street>\n" +
                "            <City>Yucksville</City>\n" +
                "            <PostalCode>xxxyyy</PostalCode>\n" +
                "        </Address>\n" +
                "    </Customer>\n" +
                "    <Customer Id=\"101\">\n" +
                "        <Name>Bill</Name>\n" +
                "        <Age>39</Age>\n" +
                "        <LastName/>\n" +
                "        <Address>\n" +
                "            <Street>10 Idle Lane</Street>\n" +
                "            <City>Yucksville</City>\n" +
                "            <PostalCode>xxxyyy</PostalCode>\n" +
                "        </Address>\n" +
                "    </Customer>\n" +
                "\n" +
                "</Customers>\n" +
                "\n";

        JsonObject json = Utils.xmlToJson(message).getObject("Customers");
        assertNotNull(json);

        String xml = Utils.jsonToXml(json, "Customers");
        assertNotNull(xml);

        testComplete();
    }
}
