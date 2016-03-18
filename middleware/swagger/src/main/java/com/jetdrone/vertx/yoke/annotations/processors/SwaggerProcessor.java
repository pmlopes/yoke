package com.jetdrone.vertx.yoke.annotations.processors;

import com.jetdrone.vertx.yoke.json.JsonSchemaResolver;
import com.jetdrone.vertx.yoke.annotations.*;
import com.jetdrone.vertx.yoke.middleware.Swagger;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class SwaggerProcessor extends AbstractAnnotationHandler<Swagger> {

    public SwaggerProcessor() {
        super(Swagger.class);
    }

    @Override
    public void process(Swagger swagger, Object instance, Class<?> clazz, Method method) {

        StringBuilder sb;

        SwaggerResource res = Processor.getAnnotation(clazz, SwaggerResource.class);

        if (res == null) {
            return;
        }

        // create the resource
        final Swagger.Resource resource = swagger.createResource(res.path(), res.description());

        Produces clazzProducesAnn = Processor.getAnnotation(clazz, Produces.class);
        Consumes clazzConsumesAnn = Processor.getAnnotation(clazz, Consumes.class);

        String[] clazzProduces = clazzProducesAnn != null ? clazzProducesAnn.value() : null;
        String[] clazzConsumes = clazzConsumesAnn != null ? clazzConsumesAnn.value() : null;

        if (clazzProduces != null) {
            resource.produces(clazzProduces);
        }

        if (clazzConsumes != null) {
            resource.consumes(clazzConsumes);
        }

        Produces producesAnn = Processor.getAnnotation(method, Produces.class);
        Consumes consumesAnn = Processor.getAnnotation(method, Consumes.class);

        String[] produces = producesAnn != null ? producesAnn.value() : null;
        String[] consumes = consumesAnn != null ? consumesAnn.value() : null;

        if (produces == null) {
            produces = clazzProduces;
        }
        if (consumes == null) {
            consumes = clazzConsumes;
        }

        // register the produces
        if (produces != null) {
            resource.produces(produces);
        }

        // register the consumes
        if (consumes != null) {
            resource.consumes(consumes);
        }

        for (JsonSchema model : res.models()) {
            String id = model.id();
            JsonObject json = new JsonObject(JsonSchemaResolver.resolveSchema(model.value()));

            if ("".equals(id)) {
                // try to extract from the schema itself
                id = json.getString("id");
            }
            resource.addModel(id, json);
        }

        SwaggerDoc doc = Processor.getAnnotation(method, SwaggerDoc.class);
        Deprecated deprecated = Processor.getAnnotation(method, Deprecated.class);

        if (doc == null) {
            return;
        }

        // create operations json
        final JsonObject operations = new JsonObject();

        // add all notes
        if (doc.notes().length > 0) {
            sb = new StringBuilder();
            for (String s : doc.notes()) {
                sb.append(s);
                sb.append(' ');
            }

            String finalNotes = sb.toString();
            int len = finalNotes.length() - 1;
            len = len > 0 ? len : 0;
            finalNotes = finalNotes.substring(0, len);
            operations.put("notes", finalNotes);
        }

        // add nickname (deducted from the method name)
        operations.put("nickname", method.getName());

        DataType returnType = doc.type();

        if (returnType == DataType.REF) {
            operations.put("type", doc.refId());
        } else {
            operations.put("type", returnType.type());
            String format = returnType.format();
            if (format != null) {
                operations.put("format", format);
            }
        }

        // TODO: authorizations

        // add parameters
        if (doc.parameters().length > 0) {
            JsonArray jsonParameters = new JsonArray();
            operations.put("parameters", jsonParameters);

            for (Parameter parameter : doc.parameters()) {
                jsonParameters.add(parseParameter(parameter, clazzConsumesAnn, consumesAnn));
            }
        }

        // add response messages
        if (doc.responseMessages().length > 0) {
            JsonArray jsonResponseMessages = new JsonArray();
            operations.put("responseMessages", jsonResponseMessages);

            for (ResponseMessage responseMessage : doc.responseMessages()) {
                JsonObject json = new JsonObject()
                        .put("code", responseMessage.code())
                        .put("message", responseMessage.message());

                if (!responseMessage.responseModel().equals("")) {
                    json.put("responseModel", responseMessage.responseModel());
                }

                jsonResponseMessages.add(json);
            }
        }

        // produces
        if (produces != null) {
            operations.put("produces", new JsonArray(Arrays.asList(produces)));
        }

        // consumes
        if (consumes != null) {
            operations.put("consumes", new JsonArray(Arrays.asList(consumes)));
        }

        if (deprecated != null) {
            // TODO: once SWAGGER API changes this should be boolean
            operations.put("deprecated", "true");
        }

        // process the methods that have both YokeRequest and Handler

        if (Processor.isCompatible(method, ALL.class, YokeRequest.class, Handler.class)) {
            resource.all(Processor.getAnnotation(method, ALL.class).value(), doc.summary(), operations);
        }
        if (Processor.isCompatible(method, CONNECT.class, YokeRequest.class, Handler.class)) {
            resource.connect(Processor.getAnnotation(method, CONNECT.class).value(), doc.summary(), operations);
        }
        if (Processor.isCompatible(method, OPTIONS.class, YokeRequest.class, Handler.class)) {
            resource.options(Processor.getAnnotation(method, OPTIONS.class).value(), doc.summary(), operations);
        }
        if (Processor.isCompatible(method, HEAD.class, YokeRequest.class, Handler.class)) {
            resource.head(Processor.getAnnotation(method, HEAD.class).value(), doc.summary(), operations);
        }
        if (Processor.isCompatible(method, GET.class, YokeRequest.class, Handler.class)) {
            resource.get(Processor.getAnnotation(method, GET.class).value(), doc.summary(), operations);
        }
        if (Processor.isCompatible(method, POST.class, YokeRequest.class, Handler.class)) {
            resource.post(Processor.getAnnotation(method, POST.class).value(), doc.summary(), operations);
        }
        if (Processor.isCompatible(method, PUT.class, YokeRequest.class, Handler.class)) {
            resource.put(Processor.getAnnotation(method, PUT.class).value(), doc.summary(), operations);
        }
        if (Processor.isCompatible(method, PATCH.class, YokeRequest.class, Handler.class)) {
            resource.patch(Processor.getAnnotation(method, PATCH.class).value(), doc.summary(), operations);
        }
        if (Processor.isCompatible(method, DELETE.class, YokeRequest.class, Handler.class)) {
            resource.delete(Processor.getAnnotation(method, DELETE.class).value(), doc.summary(), operations);
        }
    }

    @Override
    public void process(Swagger swagger, Object instance, Class<?> clazz, Field field) {
        // NOOP
    }

    private JsonObject parseParameter(Parameter parameter, Consumes classConsumes, Consumes methodConsumes) {

        final JsonObject response = new JsonObject();

        // must be lower case
        response.put("paramType", parameter.paramType().name().toLowerCase());
        response.put("name", parameter.name());
        // recommended
        String description = parameter.description();
        if (!description.equals("")) {
            response.put("description", parameter.description());
        }
        // optional
        response.put("required", parameter.required());
        response.put("allowMultiple", parameter.allowMultiple());

        // describe the type
        final DataType type = parameter.type();

        switch (type) {
            case REF:
                response.put("type", parameter.modelRef());
                break;
            // primitives
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE:
                response.put("type", type.type());
                if (type.format() != null) {
                    response.put("format", type.format());
                }
                if (!parameter.minimum().equals("")) {
                    response.put("minimum", parameter.minimum());
                }
                if (!parameter.maximum().equals("")) {
                    response.put("maximum", parameter.maximum());
                }
                if (!parameter.defaultValue().equals("")) {
                    String val = parameter.defaultValue();
                    if (val.indexOf('.') != -1) {
                        response.put("defaultValue", Double.parseDouble(parameter.defaultValue()));
                    } else {
                        response.put("defaultValue", Integer.parseInt(parameter.defaultValue()));
                    }
                }
                break;
            case BYTE:
            case STRING:
            case DATE:
            case DATETIME:
                response.put("type", type.type());
                if (type.format() != null) {
                    response.put("format", type.format());
                }
                if (!parameter.defaultValue().equals("")) {
                    response.put("defaultValue", parameter.defaultValue());
                }
                if (!parameter.minimum().equals("")) {
                    response.put("minimum", parameter.minimum());
                }
                if (!parameter.maximum().equals("")) {
                    response.put("maximum", parameter.maximum());
                }
                if (parameter.enumeration().length > 0) {
                    JsonArray enumeration = new JsonArray();
                    response.put("enum", enumeration);
                    for (String item : parameter.enumeration()) {
                        enumeration.add(item);
                    }
                }
                break;
            case BOOLEAN:
                response.put("type", type.type());
                if (!parameter.defaultValue().equals("")) {
                    response.put("defaultValue", Boolean.parseBoolean(parameter.defaultValue()));
                }
                break;
            // containers
            case SET:
                response.put("uniqueItems", true);
            case ARRAY:
                response.put("type", type.type());
                if (parameter.items() == DataType.UNDEFINED) {
                    if (!"".equals(parameter.itemsRefId())) {
                        response.put("items", new JsonObject().put("$ref", parameter.itemsRefId()));
                    } else {
                        throw new RuntimeException("ARRAY/SET must specify items type or items refId");
                    }
                } else {
                    if (parameter.items() == DataType.ARRAY || parameter.items() == DataType.SET) {
                        throw new RuntimeException("ARRAY/SET cannot contain ARRAYS/SETs");
                    } else {
                        if (parameter.items() == DataType.REF) {
                            response.put("items", new JsonObject()
                                    .put("$ref", parameter.modelRef()));
                        } else {
                            response.put("items", new JsonObject()
                                    .put("type", parameter.items().type())
                                    .put("format", parameter.items().format()));
                        }
                    }
                }
                break;
            // void
            case VOID:
                response.put("type", type.type());
                break;
            // file
            case FILE:
                response.put("type", type.type());
                if (parameter.paramType() != ParamType.FORM) {
                    throw new RuntimeException("File requires paramType to be FORM");
                }
                // check that method consumes "multipart/form-data"
                boolean multipart = false;

                if (classConsumes != null) {
                    for (String c : classConsumes.value()) {
                        if ("multipart/form-data".equalsIgnoreCase(c)) {
                            multipart = true;
                            break;
                        }
                    }
                }
                if (methodConsumes != null) {
                    for (String c : methodConsumes.value()) {
                        if ("multipart/form-data".equalsIgnoreCase(c)) {
                            multipart = true;
                            break;
                        }
                    }
                }

                if (!multipart) {
                    throw new RuntimeException("File requires @Consumes(\"multipart/form-data\")");
                }

                break;
        }

        return response;
    }
}
