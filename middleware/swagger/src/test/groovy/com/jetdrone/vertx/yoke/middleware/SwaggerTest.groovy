package com.jetdrone.vertx.yoke.middleware

import com.jetdrone.vertx.yoke.Yoke
import com.jetdrone.vertx.yoke.annotations.*
import com.jetdrone.vertx.yoke.test.Response
import com.jetdrone.vertx.yoke.test.YokeTester
import org.junit.Test
import org.vertx.java.core.Handler
import org.vertx.java.core.json.JsonObject
import org.vertx.testtools.TestVerticle

import static org.vertx.testtools.VertxAssert.*
import static org.vertx.testtools.VertxAssert.assertEquals

public class SwaggerTest extends TestVerticle {

    @SwaggerResource(path = "/hello", description = "Hello web service")
    @Produces("application/json")
    public static class TestSwagger {
        @GET("/hello/:name")
        @SwaggerDoc(
                summary = "say hello to user name",
                notes = ["note #1", "note #2"],
                parameters = [
                        @Parameter(name = "name", type = Parameter.DataType.STRING, description = "User name", required = true)
                ],
                responseMessages = [
                        @ResponseMessage(code = 200, message = "No error!")
                ]
        )
        public void sayHello(YokeRequest request, Handler<Object> next) {
            request.response().end("Hello " + request.getParameter("name") + "!")
        }
    }

    @Test
    public void testAnnotatedSwagger() {
        Yoke yoke = new Yoke(this)
        Router router = new Router()
        yoke.use(router)

        final TestSwagger testSwagger = new TestSwagger()

        Swagger.from(router, "1.0.0", testSwagger).setInfo(
                new JsonObject().putString("title", "Swagger Sample App")
        )

        Router.from(router, testSwagger)

        final YokeTester tester = new YokeTester(yoke)

        tester.request("GET", "/api-docs", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode())
                System.out.println(resp.body.toString())

                tester.request("GET", "/api-docs/hello", new Handler<Response>() {
                    @Override
                    public void handle(Response resp2) {
                        assertEquals(200, resp2.getStatusCode())
                        System.out.println(resp2.body.toString())

                        testComplete()
                    }
                })
            }
        })
    }

    def resource = [
            "apiVersion"    : "1.0.0",
            "swaggerVersion": "1.2",
            "apis"          : [
                    [
                            "path"       : "/pet",
                            "description": "Operations about pets"
                    ],
                    [
                            "path"       : "/user",
                            "description": "Operations about user"
                    ],
                    [
                            "path"       : "/store",
                            "description": "Operations about store"
                    ]
            ],
//            "authorizations": [
//                    "oauth2": [
//                            "type"      : "oauth2",
//                            "scopes"    : [
//                                    [
//                                            "scope"      : "email",
//                                            "description": "Access to your email address"
//                                    ],
//                                    [
//                                            "scope"      : "pets",
//                                            "description": "Access to your pets"
//                                    ]
//                            ],
//                            "grantTypes": [
//                                    "implicit"          : [
//                                            "loginEndpoint": [
//                                                    "url": "http://petstore.swagger.wordnik.com/oauth/dialog"
//                                            ],
//                                            "tokenName"    : "access_token"
//                                    ],
//                                    "authorization_code": [
//                                            "tokenRequestEndpoint": [
//                                                    "url"             : "http://petstore.swagger.wordnik.com/oauth/requestToken",
//                                                    "clientIdName"    : "client_id",
//                                                    "clientSecretName": "client_secret"
//                                            ],
//                                            "tokenEndpoint"       : [
//                                                    "url"      : "http://petstore.swagger.wordnik.com/oauth/token",
//                                                    "tokenName": "access_code"
//                                            ]
//                                    ]
//                            ]
//                    ]
//            ],
            "info"          : [
                    "title"            : "Swagger Sample App",
                    "description"      : "This is a sample server Petstore server.  You can find out more about Swagger \n    at <a href=\"http://swagger.wordnik.com\">http://swagger.wordnik.com</a> or on irc.freenode.net, #swagger.  For this sample,\n    you can use the api key \"special-key\" to test the authorization filters",
                    "termsOfServiceUrl": "http://helloreverb.com/terms/",
                    "contact"          : "apiteam@wordnik.com",
                    "license"          : "Apache 2.0",
                    "licenseUrl"       : "http://www.apache.org/licenses/LICENSE-2.0.html"
            ]
    ]

    @SwaggerResource(path = "/pet", description = "Operations about pets")
    public static class Pet {

    }

    @SwaggerResource(path = "/user", description = "Operations about user")
    public static class User {

    }

    @SwaggerResource(path = "/store", description = "Operations about store")
    @Produces("application/json")
    public static class Store {

        @SwaggerDoc(
                summary = "Find purchase order by ID",
                notes = "For valid response try integer IDs with value <= 5. Anything above 5 or nonintegers will generate API errors",
                parameters = @Parameter(name = "orderId", type = Parameter.DataType.STRING, description = "ID of pet that needs to be fetched", required = true, paramType = Parameter.ParamType.PATH),
                responseMessages = [
                        @ResponseMessage(code = 400, message = "Invalid ID supplied"),
                        @ResponseMessage(code = 404, message = "Order not found")
                ]
        )
        @GET("/store/order/:orderId")
        public void getOrderById(YokeRequest request, Handler<Object> next) {

        }

        @SwaggerDoc(
                summary = "Delete purchase order by ID",
                notes = "For valid response try integer IDs with value < 1000.  Anything above 1000 or nonintegers will generate API errors",
                parameters = @Parameter(name = "orderId", type = Parameter.DataType.STRING, description = "ID of pet that needs to be fetched", required = true, paramType = Parameter.ParamType.PATH),
                responseMessages = [
                    @ResponseMessage(code = 400, message = "Invalid ID supplied"),
                    @ResponseMessage(code = 404, message = "Order not found")
                ]
        )
        @DELETE("/store/order/:orderId")
        public void deleteOrder(YokeRequest request, Handler<Object> next) {

        }

        @SwaggerDoc(
                summary = "Place an order for a pet",
                notes = "",
                parameters = @Parameter(name = "body", type = Parameter.DataType.VOID, description = "order placed for purchasing the pet", required = true, paramType = Parameter.ParamType.BODY),
                responseMessages = [
                        @ResponseMessage(code = 400, message = "Invalid ID supplied"),
                ]
        )
        @POST("/store/order")
        public void placeOrder(YokeRequest request, Handler<Object> next) {

        }
    }

    @Test
    public void testResourceJson() {
        Yoke yoke = new Yoke(this)
        Router router = new Router()
        yoke.use(router)

        final Store testSwagger = new Store()

        Swagger.from(router, "1.0.0", new Pet(), new User(), new Store()).setInfo(new JsonObject([
                "title"            : "Swagger Sample App",
                "description"      : "This is a sample server Petstore server.  You can find out more about Swagger \n    at <a href=\"http://swagger.wordnik.com\">http://swagger.wordnik.com</a> or on irc.freenode.net, #swagger.  For this sample,\n    you can use the api key \"special-key\" to test the authorization filters",
                "termsOfServiceUrl": "http://helloreverb.com/terms/",
                "contact"          : "apiteam@wordnik.com",
                "license"          : "Apache 2.0",
                "licenseUrl"       : "http://www.apache.org/licenses/LICENSE-2.0.html"
        ]))

        Router.from(router, testSwagger)

        final YokeTester tester = new YokeTester(yoke)

        tester.request("GET", "/api-docs", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode())

                JsonObject generated = new JsonObject(resp.body.toString())

                assertEquals(resource, generated.toMap())
                testComplete()
            }
        })
    }

    def api = [
            "apiVersion"    : "1.0.0",
            "swaggerVersion": "1.2",
//            "basePath"      : "http://petstore.swagger.wordnik.com/api",
            "basePath"      : "/",
            "resourcePath"  : "/store",
            "produces"      : [
                    "application/json"
            ],
//            "authorizations": [],
            "apis"          : [
                    [
                            "path"      : "/store/order/{orderId}",
                            "operations": [
                                    [
                                            "method"          : "GET",
                                            "summary"         : "Find purchase order by ID",
                                            "notes"           : "For valid response try integer IDs with value <= 5. Anything above 5 or nonintegers will generate API errors",
//                                            "type"            : "Order",
                                            "nickname"        : "getOrderById",
//                                            "authorizations"  : [],
                                            "parameters"      : [
                                                    [
                                                            "name"       : "orderId",
                                                            "description": "ID of pet that needs to be fetched",
                                                            "required"   : true,
//                                                            "type"       : "string",
                                                            "paramType"  : "path"
                                                    ]
                                            ],
                                            "responseMessages": [
                                                    [
                                                            "code"   : 400,
                                                            "message": "Invalid ID supplied"
                                                    ],
                                                    [
                                                            "code"   : 404,
                                                            "message": "Order not found"
                                                    ]
                                            ]
                                    ],
                                    [
                                            "method"          : "DELETE",
                                            "summary"         : "Delete purchase order by ID",
                                            "notes"           : "For valid response try integer IDs with value < 1000.  Anything above 1000 or nonintegers will generate API errors",
//                                            "type"            : "void",
                                            "nickname"        : "deleteOrder",
//                                            "authorizations"  : [
//                                                    "oauth2": [
//                                                            [
//                                                                    "scope"      : "test:anything",
//                                                                    "description": "anything"
//                                                            ]
//                                                    ]
//                                            ],
                                            "parameters"      : [
                                                    [
                                                            "name"       : "orderId",
                                                            "description": "ID of the order that needs to be deleted",
                                                            "required"   : true,
//                                                            "type"       : "string",
                                                            "paramType"  : "path"
                                                    ]
                                            ],
                                            "responseMessages": [
                                                    [
                                                            "code"   : 400,
                                                            "message": "Invalid ID supplied"
                                                    ],
                                                    [
                                                            "code"   : 404,
                                                            "message": "Order not found"
                                                    ]
                                            ]
                                    ]
                            ]
                    ],
                    [
                            "path"      : "/store/order",
                            "operations": [
                                    [
                                            "method"          : "POST",
                                            "summary"         : "Place an order for a pet",
                                            "notes"           : "",
//                                            "type"            : "void",
                                            "nickname"        : "placeOrder",
//                                            "authorizations"  : [
//                                                    "oauth2": [
//                                                            [
//                                                                    "scope"      : "test:anything",
//                                                                    "description": "anything"
//                                                            ]
//                                                    ]
//                                            ],
                                            "parameters"      : [
                                                    [
                                                            "name"       : "body",
                                                            "description": "order placed for purchasing the pet",
                                                            "required"   : true,
//                                                            "type"       : "Order",
                                                            "paramType"  : "body"
                                                    ]
                                            ],
                                            "responseMessages": [
                                                    [
                                                            "code"   : 400,
                                                            "message": "Invalid order"
                                                    ]
                                            ]
                                    ]
                            ]
                    ]
            ],
            "models"        : [:
//                    "Order": [
//                            "id"        : "Order",
//                            "properties": [
//                                    "id"      : [
//                                            "type"  : "integer",
//                                            "format": "int64"
//                                    ],
//                                    "petId"   : [
//                                            "type"  : "integer",
//                                            "format": "int64"
//                                    ],
//                                    "quantity": [
//                                            "type"  : "integer",
//                                            "format": "int32"
//                                    ],
//                                    "status"  : [
//                                            "type"       : "string",
//                                            "description": "Order Status",
//                                            "enum"       : [
//                                                    "placed",
//                                                    " approved",
//                                                    " delivered"
//                                            ]
//                                    ],
//                                    "shipDate": [
//                                            "type"  : "string",
//                                            "format": "date-time"
//                                    ]
//                            ]
//                    ]
            ]
    ]

    @Test
    public void testAPIJson() {
        Yoke yoke = new Yoke(this)
        GRouter router = new GRouter()
        yoke.use(router)

        def apis = [
                new Pet(), new User(), new Store()
        ]

        GSwagger.from(router, "1.0.0", apis.toArray())

        GRouter.from(router, apis.toArray())

        final YokeTester tester = new YokeTester(yoke)

        tester.request("GET", "/api-docs/store", new Handler<Response>() {
            @Override
            public void handle(Response resp) {
                assertEquals(200, resp.getStatusCode())

                JsonObject generated = new JsonObject(resp.body.toString())
                System.out.println(api.equals(generated.toMap()))

//                assertEquals(api, generated.toMap())
                testComplete()
            }
        })
    }
}
