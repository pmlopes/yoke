package com.jetdrone.vertx.yoke.test;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.util.Utils;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

import static org.vertx.testtools.VertxAssert.*;

public class MiddlewareTest extends TestVerticle {

    @Test
    public void testMiddleware() {
        final YokeTester yoke = new YokeTester(vertx);
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
    public void testXml() throws Exception {
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

        String s = Utils.readResourceToBuffer(Utils.class, "xml-to-json.xsl").toString();

        StreamSource xslSource = new StreamSource(new StringReader(s));

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(xslSource);

        transformer.transform( new StreamSource(new StringReader(message))
                , new StreamResult(System.out)
        );

        testComplete();

    }
}
