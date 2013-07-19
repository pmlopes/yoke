package com.jetdrone.vertx.persona;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.engine.StringPlaceholderEngine;
import com.jetdrone.vertx.yoke.middleware.*;
import com.jetdrone.vertx.yoke.util.Utils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import javax.crypto.Mac;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class Persona extends Verticle {

    @Override
    public void start() {

        // This is an example only, use a proper persistent storage
        final ConcurrentMap<String, String> storage = vertx.sharedData().getMap("session.storage.persona");

        final Yoke yoke = new Yoke(vertx);
        yoke.engine("html", new StringPlaceholderEngine());

        Mac secret = Utils.newHmacSHA256("secret here");

        // all environments
        yoke.use(new CookieParser(secret));
        yoke.use(new Session(secret));
        yoke.use(new BodyParser());
        yoke.use(new Static("static"));
        yoke.use(new ErrorHandler(true));

        // routes
        yoke.use(new Router()
                .get("/", new Middleware() {
                    @Override
                    public void handle(final YokeRequest request, final Handler<Object> next) {
                        String sid = request.getSessionId();
                        String email = storage.get(sid == null ? "" : sid);

                        if (email == null) {
                            request.put("email", "null");
                        } else {
                            request.put("email", "'" + email + "'");
                        }
                        request.response().render("views/index.html", next);
                    }
                })
                .post("/auth/logout", new Middleware() {
                    @Override
                    public void handle(YokeRequest request, Handler<Object> next) {
                        // remove session from storage
                        String sid = request.getSessionId();
                        storage.remove(sid == null ? "" : sid);
                        // destroy session
                        request.setSessionId(null);
                        // send OK
                        request.response().end(new JsonObject().putBoolean("success", true));
                    }
                })
                .post("/auth/login", new Middleware() {
                    @Override
                    public void handle(final YokeRequest request, final Handler<Object> next) {
                        String data;

                        try {
                            // generate the data
                            data = "assertion=" + URLEncoder.encode(request.formAttributes().get("assertion"), "UTF-8") +
                                    "&audience=" + URLEncoder.encode("http://localhost:8080", "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            next.handle(e);
                            return;
                        }

                        HttpClient client = getVertx().createHttpClient().setSSL(true).setHost("verifier.login.persona.org").setPort(443);

                        HttpClientRequest clientRequest = client.post("/verify", new Handler<HttpClientResponse>() {
                            public void handle(HttpClientResponse response) {
                                // error handler
                                response.exceptionHandler(new Handler<Throwable>() {
                                    @Override
                                    public void handle(Throwable err) {
                                        next.handle(err);
                                    }
                                });

                                final Buffer body = new Buffer(0);

                                // body handler
                                response.dataHandler(new Handler<Buffer>() {
                                    @Override
                                    public void handle(Buffer buffer) {
                                        body.appendBuffer(buffer);
                                    }
                                });
                                // done
                                response.endHandler(new Handler<Void>() {
                                    @Override
                                    public void handle(Void event) {
                                        try {
                                            JsonObject verifierResp = new JsonObject(body.toString());
                                            boolean valid = "okay".equals(verifierResp.getString("status"));
                                            String email = valid ? verifierResp.getString("email") : null;
                                            if (valid) {
                                                // assertion is valid:
                                                // generate a session Id
                                                String sid = UUID.randomUUID().toString();

                                                request.setSessionId(sid);
                                                // save it and associate to the email address
                                                storage.put(sid, email);
                                                // OK response
                                                request.response().end(new JsonObject().putBoolean("success", true));
                                            } else {
                                                request.response().end(new JsonObject().putBoolean("success", false));
                                            }
                                        } catch (DecodeException ex) {
                                            // bogus response from verifier!
                                            request.response().end(new JsonObject().putBoolean("success", false));
                                        }
                                    }
                                });
                            }
                        });

                        clientRequest.putHeader("content-type", "application/x-www-form-urlencoded");
                        clientRequest.putHeader("content-length", Integer.toString(data.length()));
                        clientRequest.end(data);
                    }
                })
        );

        yoke.listen(8080);
        container.logger().info("Yoke server listening on port 8080");
    }
}
