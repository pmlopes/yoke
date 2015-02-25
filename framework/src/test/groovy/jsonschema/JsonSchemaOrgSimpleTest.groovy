package jsonschema

import com.jetdrone.vertx.yoke.json.JsonSchema
import org.junit.Test

import static org.junit.Assert.assertTrue

class JsonSchemaOrgSimpleTest {

    @Test
    public void testSimpleProduct() {
        def product = [
                id   : 1,
                name : "A green door",
                price: 12.50,
                tags : ["home", "green"]
        ]

        assertTrue(JsonSchema.conformsSchema(product, "classpath:///jsonschema/product.json#"));
    }

    @Test
    public void testSimpleSetOfProducts() {
        def products = [
                [
                        "id"               : 2,
                        "name"             : "An ice sculpture",
                        "price"            : 12.50,
                        "tags"             : ["cold", "ice"],
                        "dimensions"       : [
                                "length": 7.0,
                                "width" : 12.0,
                                "height": 9.5
                        ],
                        "warehouseLocation": [
                                "latitude" : -78.75,
                                "longitude": 20.4
                        ]
                ],
                [
                        "id"               : 3,
                        "name"             : "A blue mouse",
                        "price"            : 25.50,
                        "dimensions"       : [
                                "length": 3.1,
                                "width" : 1.0,
                                "height": 1.0
                        ],
                        "warehouseLocation": [
                                "latitude" : 54.4,
                                "longitude": -32.7
                        ]
                ]
        ]

        assertTrue(JsonSchema.conformsSchema(products, "classpath:///jsonschema/set-of-products.json#"));
    }
}
