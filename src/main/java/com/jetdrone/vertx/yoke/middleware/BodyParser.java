package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.*;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.impl.DefaultHttpServerRequest;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.io.IOException;
import java.util.*;

public class BodyParser extends Middleware {

    private final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    @Override
    public void handle(final HttpServerRequest request, final Handler<Object> next) {
        // inside middleware the original request has been wrapped with yoke's
        // implementation
        final YokeHttpServerRequest req = (YokeHttpServerRequest) request;

        final String method = req.method();

        // GET and HEAD have no body
        if ("GET".equals(method) || "HEAD".equals(method)) {
            next.handle(null);
        } else {
            final String contentType = req.headers().get("content-type");

            if (contentType != null) {

                final Buffer buffer = new Buffer(0);

                req.dataHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer event) {
                        if (req.bodyLengthLimit() != -1) {
                            if (buffer.length() < req.bodyLengthLimit()) {
                                buffer.appendBuffer(event);
                            } else {
                                req.dataHandler(null);
                                req.endHandler(null);
                                next.handle(413);
                            }
                        } else {
                            buffer.appendBuffer(event);
                        }
                    }
                });

                req.endHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void _void) {
                        if (contentType.contains("application/json")) {
                            try {
                                String jsonString = buffer.toString("UTF-8");
                                if (jsonString.length() > 0) {
                                    switch (jsonString.charAt(0)) {
                                        case '{':
                                            req.body(new JsonObject(jsonString));
                                            next.handle(null);
                                            break;
                                        case '[':
                                            req.body(new JsonArray(jsonString));
                                            next.handle(null);
                                            break;
                                        default:
                                            next.handle(400);
                                    }
                                } else {
                                    next.handle(400);
                                }
                            } catch (DecodeException ex) {
                                next.handle(ex);
                            }
                        } else if (contentType.contains("application/x-www-form-urlencoded")) {
                            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(buffer.toString("UTF-8"));
                            req.body(queryStringDecoder.parameters());
                            next.handle(null);
                        } else if (contentType.contains("multipart/form-data")) {
                            try {
                                HttpRequest nettyReq = ((DefaultHttpServerRequest) req.vertxHttpServerRequest()).nettyRequest();
                                HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, nettyReq);

                                decoder.offer(new DefaultHttpContent(buffer.getByteBuf()));
                                decoder.offer(LastHttpContent.EMPTY_LAST_CONTENT);

                                for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                                    switch (data.getHttpDataType()) {
                                        case Attribute:
                                            if (req.body() == null) {
                                                req.body(new HashMap<>());
                                            }
                                            final Attribute attribute = (Attribute) data;
                                            final Map<Object, Object> mapBody = (Map<Object, Object>) req.body();

                                            Object value = mapBody.get(attribute.getName());
                                            if (value == null) {
                                                mapBody.put(attribute.getName(), attribute.getValue());
                                            } else {
                                                if (value instanceof List) {
                                                    ((List<String>) value).add(attribute.getValue());
                                                } else {
                                                    List<String> l = new ArrayList<>();
                                                    l.add((String) value);
                                                    l.add(attribute.getValue());
                                                    mapBody.put(attribute.getName(), l);
                                                }
                                            }
                                            break;
                                        case FileUpload:
                                            if (req.files() == null) {
                                                req.files(new HashMap<String, FileUpload>());
                                            }
                                            FileUpload fileUpload = (FileUpload) data;
                                            req.files().put(fileUpload.getName(), fileUpload);
                                            break;
                                        default:
                                            System.out.println(data);
                                    }
                                }

                                next.handle(null);
                                // clean up
                                decoder.cleanFiles();
                            } catch (ErrorDataDecoderException | NotEnoughDataDecoderException | IncompatibleDataDecoderException | IOException e) {
                                next.handle(e);
                            }
                        } else {
                            next.handle(null);
                        }
                    }
                });
            }
        }
    }
}
