/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetdrone.vertx.yoke.middleware;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
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
import java.util.Map;
import java.util.Set;

public class YokeHttpServerRequest implements HttpServerRequest {

    // the original request
    private final HttpServerRequest request;
    // the wrapped response
    private final YokeHttpServerResponse response;
    // the request context
    private final Map<String, Object> context;
    // is this request secure
    // TODO: get me right
    private final boolean secure = false;

    // we can overrride the setMethod
    private String method;
    private long bodyLengthLimit = -1;
    private Object body;
    private Map<String, FileUpload> files;
    private Set<Cookie> cookies;
    private String sessionId;

    public YokeHttpServerRequest(HttpServerRequest request, YokeHttpServerResponse response, Map<String, Object> context) {
        this.context = context;
        this.request = request;
        this.method = request.method();
        this.response = response;
    }

    /**
     * Allow getting properties in a generified way.
     *
     * @param name The key to get
     * @param <R> The type of the return
     * @return The found object
     */
    @SuppressWarnings("unchecked")
    public <R> R get(String name) {
        return (R) context.get(name);
    }

    /**
     * Allow getting properties in a generified way and return defaultValue if the key does not exist.
     *
     * @param name The key to get
     * @param defaultValue value returned when the key does not exist
     * @param <R> The type of the return
     * @return The found object
     */
    public <R> R get(String name, R defaultValue) {
        if (context.containsKey(name)) {
            return get(name);
        } else {
            return defaultValue;
        }
    }

    /**
     * Allows putting a value into the context
     *
     * @param name the key to store
     * @param value the value to store
     * @param <R> the type of the previous value if present
     * @return the previous value or null
     */
    @SuppressWarnings("unchecked")
    public <R> R put(String name, Object value) {
        return (R) context.put(name, value);
    }

    /**
     * Allow getting headers in a generified way.
     *
     * @param name The key to get
     * @param <R> The type of the return
     * @return The found object
     */
    @SuppressWarnings("unchecked")
    public <R> R getHeader(String name) {
        return (R) headers().get(name);
    }

    /**
     * Allow getting headers in a generified way and return defaultValue if the key does not exist.
     *
     * @param name The key to get
     * @param defaultValue value returned when the key does not exist
     * @param <R> The type of the return
     * @return The found object
     */
    public <R> R getHeader(String name, R defaultValue) {
        if (headers().containsKey(name)) {
            return getHeader(name);
        } else {
            return defaultValue;
        }
    }

    /**
     * The original HTTP setMethod for the request. One of GET, PUT, POST, DELETE, TRACE, CONNECT, OPTIONS or HEAD
     */
    public String originalMethod() {
        return request.method();
    }

    /**
     * Package level mutator for the overrided setMethod
     * @param newMethod new setMethod GET, PUT, POST, DELETE, TRACE, CONNECT, OPTIONS or HEAD
     */
    void setMethod(String newMethod) {
        this.method = newMethod.toUpperCase();
    }

    /**
     * Package level mutator for the bodyLength
     */
    void setBodyLengthLimit(long limit) {
        bodyLengthLimit = limit;
    }

    /**
     * Holds the maximum allowed length for the setBody data. -1 for unlimited
     */
    public long bodyLengthLimit() {
        return bodyLengthLimit;
    }

    /**
     * Returns true if this request has setBody
     *
     * @return true if content-length or transfer-encoding is present
     */
    public boolean hasBody() {
        Map<String, String> headers = headers();
        return headers.containsKey("transfer-encoding") || headers.containsKey("content-length");
    }

    /**
     * Returns the content length of this request setBody or -1 if header is not present.
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
     * The request setBody and eventually a parsed version of it in json or map
     */
    public Object body() {
        return body;
    }

    /**
     * The request setBody and eventually a parsed version of it in json or map
     */
    public JsonObject jsonBody() {
        if (body != null && body instanceof JsonObject) {
            return (JsonObject) body;
        }
        return null;
    }

    /**
     * The request setBody and eventually a parsed version of it in json or map
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> mapBody() {
        if (body != null && body instanceof Map) {
            return (Map<String, String>) body;
        }
        return null;
    }

    /**
     * The request setBody and eventually a parsed version of it in json or map
     */
    public Buffer bufferBody() {
        if (body != null && body instanceof Buffer) {
            return (Buffer) body;
        }
        return null;
    }

    /**
     * Mutator for the request setBody
     * The request setBody and eventually a parsed version of it in json or map
     */
    void setBody(Object body) {
        this.body = body;
    }

    /**
     * The uploaded setFiles
     */
    public Map<String, FileUpload> files() {
        return files;
    }

    /**
     * The uploaded setFiles
     */
    void setFiles(Map<String, FileUpload> files) {
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
    void setCookies(Set<Cookie> cookies) {
        this.cookies = cookies;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String sessionId() {
        return sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isSecure() {
        return secure;
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

    public HttpVersion protocolVersion() {
        return nettyRequest().getProtocolVersion();
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
        request.bodyHandler(bodyHandler);
        return this;
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#dataHandler(org.vertx.java.core.Handler)
     */
    @Override
    public HttpServerRequest dataHandler(Handler<Buffer> handler) {
        request.dataHandler(handler);
        return this;
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#pause()
     */
    @Override
    public HttpServerRequest pause() {
        request.pause();
        return this;
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#resume()
     */
    @Override
    public HttpServerRequest resume() {
        request.resume();
        return this;
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#endHandler(org.vertx.java.core.Handler)
     */
    @Override
    public HttpServerRequest endHandler(Handler<Void> endHandler) {
        request.endHandler(endHandler);
        return this;
    }

    /**
     * @see org.vertx.java.core.http.HttpServerRequest#exceptionHandler(org.vertx.java.core.Handler)
     */
    @Override
    public HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
        request.exceptionHandler(handler);
        return this;
    }

    // JavaBean like accessors helpers for other language bindings such as Groovy

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

    public YokeHttpServerResponse getResponse() {
        return response();
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

    public HttpVersion getProtocolVersion() {
        return protocolVersion();
    }
}
