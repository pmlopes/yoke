package com.jetdrone.vertx.yoke.annotations;

import com.jetdrone.vertx.yoke.middleware.Swagger;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SwaggerProcessor implements AnnotationHandler<Swagger> {

    @Override
    public boolean isFor(Class<?> clazz) {
        return Swagger.class.isAssignableFrom(clazz);
    }

    @Override
    public void process(Swagger swagger, Object instance, Class<?> clazz, Method method) {
        Api res = Processor.getAnnotation(clazz, Api.class);

        if (res == null) {
            return;
        }

        // create the resource
        final Swagger.Resource resource = swagger.createResource(res.path(), res.description());

        Produces clazzProducesAnn = Processor.getAnnotation(clazz, Produces.class);
        Consumes clazzConsumesAnn = Processor.getAnnotation(clazz, Consumes.class);

        String[] clazzProduces = clazzProducesAnn != null ? clazzProducesAnn.value() : null;
        String[] clazzConsumes = clazzConsumesAnn != null ? clazzConsumesAnn.value() : null;

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
            resource.consumes(produces);
        }

        ApiDoc doc = Processor.getAnnotation(method, ApiDoc.class);

        if (doc == null) {
            return;
        }

        // create operations json
        final JsonObject operations = new JsonObject();

        // add all notes
        if (doc.notes().length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : doc.notes()) {
                sb.append(s);
                sb.append(' ');
            }

            String finalNotes = sb.toString();
            int len = finalNotes.length() - 1;
            len = len > 0 ? len : 0;
            finalNotes = finalNotes.substring(0, len);
            operations.putString("notes", finalNotes);
        }

        // add nickname (deducted from the method name)
        operations.putString("nickname", method.getName());

        // add parameters
        if (doc.parameters().length > 0) {
            JsonArray jsonParameters = new JsonArray();
            operations.putArray("parameters", jsonParameters);

            for (Parameter parameter : doc.parameters()) {
                jsonParameters.addObject(
                        new JsonObject()
                                .putString("name", parameter.name())
                                .putString("description", parameter.description())
                                .putBoolean("required", parameter.required())
//                                .putString("type", parameter.type())
//                                .putString("format", parameter.format())
                                .putString("paramType", parameter.paramType().name())
                                .putBoolean("allowMultiple", parameter.allowMultiple())
//                                .putString("minimum", parameter.minimum())
//                                .putString("maximum", parameter.maximum())
                );
            }
        }

        // add response messages
        if (doc.responseMessages().length > 0) {
            JsonArray jsonResponseMessages = new JsonArray();
            operations.putArray("responseMessages", jsonResponseMessages);

            for (ResponseMessage responseMessage : doc.responseMessages()) {
                jsonResponseMessages.addObject(
                        new JsonObject()
                                .putNumber("code", responseMessage.code())
                                .putString("message", responseMessage.message())
                );
            }
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
}
