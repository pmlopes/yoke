package com.jetdrone.vertx.extras;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.extras.middleware.Swagger;
import com.jetdrone.vertx.yoke.middleware.*;
import com.jetdrone.vertx.yoke.middleware.BodyParser;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class SwaggerExample extends Verticle {

    @Override
    public void start() {
        final Yoke yoke = new Yoke(this);
        final Swagger swagger = new Swagger("1.0.0");

        swagger.setInfo(new JsonObject()
                .putString("title", "Swagger Sample App")
                .putString("description", "This is a sample server Petstore server. You can find out more about Swagger at <a href=\"http://swagger.wordnik.com\">http://swagger.wordnik.com</a> or on irc.freenode.net, #swagger. For this sample, you can use the api key \"special-key\" to test the authorization filters")
                .putString("termsOfServiceUrl", "http://helloreverb.com/terms/")
                .putString("contact", "apiteam@wordnik.com")
                .putString("license", "Apache 2.0")
                .putString("licenseUrl", "http://www.apache.org/licenses/LICENSE-2.0.html"));

        yoke.use(new BodyParser());
        yoke.use(new ErrorHandler(true));
        yoke.use("/api", swagger);
        yoke.use(new Static("swagger-ui-2.0.12"));

        Swagger.Resource pets;

        pets = swagger.createResource(yoke, "/pet", "Operations about pets")
                .produces("application/json", "application/xml", "text/plain", "text/html");

        pets.get("/pet/{petId}", "Find pet by ID", new JsonObject()
                        .putString("notes", "Returns a pet based on ID")
                        .putString("type", "Pet")
                        .putString("nickname", "getPetById")
                        .putObject("authorizations", new JsonObject())
                        .putArray("parameters", new JsonArray().addObject(new JsonObject()
                                .putString("name", "petId")
                                .putString("description", "ID of pet that needs to be fetched")
                                .putBoolean("required", true)
                                .putString("type", "integer")
                                .putString("format", "int64")
                                .putString("paramType", "path")
                                .putBoolean("allowMultiple", false)
                                .putString("minimum", "1.0")
                                .putString("maximum", "100000.0")))
                        .putArray("responseMessages", new JsonArray()
                                .addObject(new JsonObject()
                                        .putNumber("code", 400)
                                        .putString("message", "Invalid ID supplied"))

                                .addObject(new JsonObject()
                                        .putNumber("code", 404)
                                        .putString("message", "Pet not found")))
        );

        yoke.listen(8080);

        container.logger().info("Yoke server listening on port 8080");
    }
}
