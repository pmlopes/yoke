package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.annotations.*;
import org.vertx.java.core.Handler;

@SwaggerResource(
        path = "/store",
        description = "Operations about store",
        models = [
                @JsonSchema("classpath:///com/jetdrone/vertx/yoke/middleware/Order.json")
        ]
)
@Produces("application/json")
public class Store {

    @SwaggerDoc(
            summary = "Find purchase order by ID",
            notes = "For valid response try integer IDs with value <= 5. Anything above 5 or nonintegers will generate API errors",
            parameters = @Parameter(name = "orderId", type = DataType.STRING, description = "ID of pet that needs to be fetched", required = true, paramType = ParamType.PATH),
            type = DataType.REF,
            refId = "Order",
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
            parameters = @Parameter(name = "orderId", type = DataType.STRING, description = "ID of pet that needs to be fetched", required = true, paramType = ParamType.PATH),
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
            parameters = @Parameter(name = "body", type = DataType.VOID, description = "order placed for purchasing the pet", required = true, paramType = ParamType.BODY),
            responseMessages = [
                    @ResponseMessage(code = 400, message = "Invalid ID supplied"),
            ]
    )
    @POST("/store/order")
    public void placeOrder(YokeRequest request, Handler<Object> next) {

    }
}
