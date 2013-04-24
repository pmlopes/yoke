package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Engine;
import groovy.lang.Closure;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.FileUpload;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.Set;

public class GYokeHttpServerRequest extends YokeHttpServerRequest {

    private final GYokeHttpServerResponse response;

    public GYokeHttpServerRequest(HttpServerRequest request, Map<String, Object> context, Map<String, Engine> engines) {
        super(request, context);
        response = new GYokeHttpServerResponse(request.response(), context, engines);
    }

    public String getOriginalMethod() {
        return originalMethod();
    }

    public long getBodyLengthLimit() {
        return bodyLengthLimit();
    }

    public long getContentLength() {
        return contentLength();
    }

    public Object getBody() {
        return body();
    }

    public JsonObject getJsonBody() {
        return jsonBody();
    }

    public Map<String, String> getMapBody() {
        return mapBody();
    }

    public Buffer getBufferBody() {
        return bufferBody();
    }

    public Map<String, FileUpload> getFiles() {
        return files();
    }

    public Set<Cookie> getCookies() {
        return cookies();
    }

    public HttpServerRequest getVertxHttpServerRequest() {
        return vertxHttpServerRequest();
    }

    public HttpRequest getNettyRequest() {
        return nettyRequest();
    }

    HttpVersion getProtocolVersion() {
        return protocolVersion();
    }

    public String getMethod() {
        return method();
    }

    public String getUri() {
        return uri();
    }

    public String getPath() {
        return path();
    }

    public String getQuery() {
        return query();
    }

    public GYokeHttpServerResponse getResponse() {
        return response;
    }

    public Map<String, String> getHeaders() {
        return headers();
    }

    public Map<String, String> getParams() {
        return params();
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress();
    }

    public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        return peerCertificateChain();
    }

    public URI getAbsoluteURI() {
        return absoluteURI();
    }

    public GYokeHttpServerRequest bodyHandler(final Closure<Buffer> handler) {
        bodyHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer buffer) {
                handler.call(buffer);
            }
        });
        return this;
    }

    public GYokeHttpServerRequest dataHandler(final Closure<Buffer> handler) {
        dataHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer buffer) {
                handler.call(buffer);
            }
        });
        return this;
    }

    public GYokeHttpServerRequest endHandler(final Closure<Void> handler) {
        endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _void) {
                handler.call(_void);
            }
        });
        return this;
    }

    public GYokeHttpServerRequest exceptionHandler(final Closure<Throwable> handler) {
        exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable throwable) {
                handler.call(throwable);
            }
        });
        return this;
    }
}
