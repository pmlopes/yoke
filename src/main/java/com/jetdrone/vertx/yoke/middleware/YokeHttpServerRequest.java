package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Engine;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.FileUpload;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.impl.DefaultHttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class YokeHttpServerRequest extends HashMap<String, Object> implements HttpServerRequest {

    // the original request
    private final HttpServerRequest request;
    // the original response
    private final YokeHttpServerResponse response;

    // we can overrride the method
    private String method;
    private long bodyLengthLimit = -1;
    private Object body;
    private Map<String, FileUpload> files;
    private Set<Cookie> cookies;

    public YokeHttpServerRequest(HttpServerRequest request, Map<String, Object> context, Map<String, Engine> engines) {
        super(context);
        this.request = request;
        this.method = request.method();
        this.response = new YokeHttpServerResponse(request.response(), this, engines);
    }


    /**
     * The original HTTP method for the request. One of GET, PUT, POST, DELETE, TRACE, CONNECT, OPTIONS or HEAD
     */
    public String originalMethod() {
        return request.method();
    }

    /**
     * Package level mutator for the overrided method
     * @param newMethod new method GET, PUT, POST, DELETE, TRACE, CONNECT, OPTIONS or HEAD
     */
    void method(String newMethod) {
        this.method = newMethod.toUpperCase();
    }

    /**
     * Package level mutator for the bodyLength
     */
    void setBodyLengthLimit(long limit) {
        bodyLengthLimit = limit;
    }

    /**
     * Holds the maximum allowed length for the body data. -1 for unlimited
     */
    public long bodyLengthLimit() {
        return bodyLengthLimit;
    }

    /**
     * Returns true if this request has body
     *
     * @return true if content-length or transfer-encoding is present
     */
    public boolean hasBody() {
        Map<String, String> headers = headers();
        return headers.containsKey("transfer-encoding") || headers.containsKey("content-length");
    }

    /**
     * Returns the content length of this request body or -1 if header is not present.
     */
    public long contentLength() {
        String contentLengthHeader = headers().get("content-length");
        if (contentLengthHeader != null) {
            return Long.parseLong(contentLengthHeader);
        } else {
            return -1;
        }
    }

    /**
     * The request body and eventually a parsed version of it in json or map
     */
    public Object body() {
        return body;
    }

    /**
     * The request body and eventually a parsed version of it in json or map
     */
    public JsonObject jsonBody() {
        if (body != null && body instanceof JsonObject) {
            return (JsonObject) body;
        }
        return null;
    }

    /**
     * The request body and eventually a parsed version of it in json or map
     */
    public Map<String, Object> mapBody() {
        if (body != null && body instanceof Map) {
            return (Map<String, Object>) body;
        }
        return null;
    }

    /**
     * The request body and eventually a parsed version of it in json or map
     */
    public Buffer bufferBody() {
        if (body != null && body instanceof Buffer) {
            return (Buffer) body;
        }
        return null;
    }

    /**
     * Mutator for the request body
     * The request body and eventually a parsed version of it in json or map
     */
    void body(Object body) {
        this.body = body;
    }

    /**
     * The uploaded files
     */
    public Map<String, FileUpload> files() {
        return files;
    }

    /**
     * The uploaded files
     */
    void files(Map<String, FileUpload> files) {
        this.files = files;
    }

    /**
     * Cookies
     */
    public Set<Cookie> cookies() {
        return cookies;
    }

    /**
     * Cookies
     */
    void cookies(Set<Cookie> cookies) {
        this.cookies = cookies;
    }

    /**
     * Return the real request
     */
    public HttpServerRequest vertxHttpServerRequest() {
        return request;
    }

    public HttpRequest nettyRequest() {
        if (request instanceof DefaultHttpServerRequest) {
            return ((DefaultHttpServerRequest) request).nettyRequest();
        }
        return null;
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#method()
     */
    @Override
    public String method() {
        if (method != null) {
            return method;
        }
        return request.method();
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#uri()
     */
    @Override
    public String uri() {
        return request.uri();
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#path()
     */
    @Override
    public String path() {
        return request.path();
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#query()
     */
    @Override
    public String query() {
        return request.query();
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#response()
     */
    @Override
    public YokeHttpServerResponse response() {
        return response;
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#headers()
     */
    @Override
    public Map<String, String> headers() {
        return request.headers();
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#params()
     */
    @Override
    public Map<String, String> params() {
        return request.params();
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#remoteAddress()
     */
    @Override
    public InetSocketAddress remoteAddress() {
        return request.remoteAddress();
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#peerCertificateChain()
     */
    @Override
    public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
        return request.peerCertificateChain();
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#absoluteURI()
     */
    @Override
    public URI absoluteURI() {
        return request.absoluteURI();
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#bodyHandler(org.vertx.java.core.Handler)
     */
    @Override
    public HttpServerRequest bodyHandler(Handler<Buffer> bodyHandler) {
        return request.bodyHandler(bodyHandler);
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#dataHandler(org.vertx.java.core.Handler)
     */
    @Override
    public HttpServerRequest dataHandler(Handler<Buffer> handler) {
        return request.dataHandler(handler);
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#pause()
     */
    @Override
    public HttpServerRequest pause() {
        return request.pause();
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#resume()
     */
    @Override
    public HttpServerRequest resume() {
        return request.resume();
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#endHandler(org.vertx.java.core.Handler)
     */
    @Override
    public HttpServerRequest endHandler(Handler<Void> endHandler) {
        return request.endHandler(endHandler);
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#exceptionHandler(org.vertx.java.core.Handler)
     */
    @Override
    public HttpServerRequest exceptionHandler(Handler<Exception> handler) {
        return request.exceptionHandler(handler);
    }
}
