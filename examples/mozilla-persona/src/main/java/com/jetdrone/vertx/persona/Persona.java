package com.jetdrone.vertx.persona;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.engine.StringPlaceholderEngine;
import com.jetdrone.vertx.yoke.middleware.*;
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

public class Persona extends Verticle {

    @Override
    public void start() {
        final Yoke yoke = new Yoke(this);
        yoke.engine(new StringPlaceholderEngine("views"));

        final Mac secret = yoke.security().getMac("HmacSHA256");

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
                        JsonObject sessionData = request.get("session");

                        if (sessionData == null) {
                            // no session
                            request.put("email", "null");
                        } else {
                            String email = sessionData.getString("email");

                            if (email == null) {
                                request.put("email", "null");
                            } else {
                                request.put("email", "'" + email + "'");
                            }
                        }

                        request.response().render("index.shtml", next);
                    }
                })
                .post("/auth/logout", new Middleware() {
                    @Override
                    public void handle(YokeRequest request, Handler<Object> next) {
                        // destroy session
                        request.destroySession();
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
                                            // assertion is valid:
                                            if (valid) {
                                                // generate a session
                                                request.createSession();
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
