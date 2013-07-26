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
package com.jetdrone.vertx.yoke.test;

import com.jetdrone.vertx.yoke.Yoke;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.vertx.java.core.*;
import org.vertx.java.core.impl.CaseInsensitiveMultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.*;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.platform.Verticle;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class YokeTester extends Yoke {

    private final Vertx vertx;
    private final HttpServer fakeServer = new FakeHttpServer();

    public YokeTester(Verticle verticle, boolean fakeSSL) {
        super(verticle);
        this.vertx = verticle.getVertx();
        fakeServer.setSSL(fakeSSL);
        listen(fakeServer);
    }

    public YokeTester(Verticle verticle) {
        this(verticle, false);
    }

    public void request(final String method, final String url, final Handler<Response> handler) {
        request(method, url, new CaseInsensitiveMultiMap(), false, new Buffer(0), handler);
    }

    public void request(final String method, final String url, final MultiMap headers, final Handler<Response> handler) {
        request(method, url, headers, false, new Buffer(0), handler);
    }
    public void request(final String method, final String url, final MultiMap headers, final Buffer body, final Handler<Response> handler) {
        request(method, url, headers, false, body, handler);
    }

    public void request(final String method, final String url, final MultiMap headers, final boolean urlEncoded, final Buffer body, final Handler<Response> handler) {
        try {
            final URI uri = new URI(url);

            final Response response = new Response(vertx, handler);

            // start yoke
            fakeServer.requestHandler().handle(new HttpServerRequest() {

                MultiMap params = null;
                MultiMap attributes = null;

                @Override
                public HttpVersion version() {
                    return HttpVersion.HTTP_1_1;
                }

                @Override
                public String method() {
                    return method.toUpperCase();
                }

                @Override
                public String uri() {
                    return uri.getPath() + "?" + uri.getQuery() + "#" + uri.getFragment();
                }

                @Override
                public String path() {
                    return uri.getPath();
                }

                @Override
                public String query() {
                    return uri.getQuery();
                }

                @Override
                public HttpServerResponse response() {
                    return response;
                }

                @Override
                public MultiMap headers() {
                    return headers;
                }

                @Override
                public MultiMap params() {
                    if (params == null) {
                        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri());
                        Map<String, List<String>> prms = queryStringDecoder.parameters();
                        params = new CaseInsensitiveMultiMap();

                        if (!prms.isEmpty()) {
                            for (Map.Entry<String, List<String>> entry: prms.entrySet()) {
                                params.add(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                    return params;
                }

                @Override
                public InetSocketAddress remoteAddress() {
                    return new InetSocketAddress("127.0.0.1", 80);
                }

                @Override
                public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
                    return null;
                }

                @Override
                public URI absoluteURI() {
                    return uri;
                }

                @Override
                public HttpServerRequest bodyHandler(Handler<Buffer> bodyHandler) {
                    bodyHandler.handle(body);
                    return this;
                }

                @Override
                public HttpServerRequest dataHandler(Handler<Buffer> handler) {
                    handler.handle(body);
                    return this;
                }

                @Override
                public HttpServerRequest pause() {
                    throw new UnsupportedOperationException("This mock does not support pause");
                }

                @Override
                public HttpServerRequest resume() {
                    throw new UnsupportedOperationException("This mock does not support resume");
                }

                @Override
                public HttpServerRequest endHandler(Handler<Void> endHandler) {
                    endHandler.handle(null);
                    return this;
                }
                @Override
                public NetSocket netSocket() {
                    throw new UnsupportedOperationException("This mock does not support netSocket");
                }

                @Override
                public HttpServerRequest expectMultiPart(boolean expect) {
                    // NOOP
                    return this;
                }

                @Override
                public HttpServerRequest uploadHandler(Handler<HttpServerFileUpload> uploadHandler) {
                    throw new UnsupportedOperationException("This mock does not support uploadHandler");
                }

                @Override
                public MultiMap formAttributes() {
                    if (attributes == null) {
                        attributes = new CaseInsensitiveMultiMap();
                        if (urlEncoded) {
                            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(body.toString(), false);

                            Map<String, List<String>> prms = queryStringDecoder.parameters();

                            if (!prms.isEmpty()) {
                                for (Map.Entry<String, List<String>> entry: prms.entrySet()) {
                                    attributes.add(entry.getKey(), entry.getValue());
                                }
                            }
                        }
                    }
                    return attributes;
                }

                @Override
                public HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
                    throw new UnsupportedOperationException("This mock does not support exceptionHandler");
                }
            });
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static class FakeHttpServer implements HttpServer {

        Handler<HttpServerRequest> requestHandler;
        boolean ssl = false;

        @Override
        public HttpServer requestHandler(Handler<HttpServerRequest> requestHandler) {
            this.requestHandler = requestHandler;
            return this;
        }

        @Override
        public Handler<HttpServerRequest> requestHandler() {
            return requestHandler;
        }

        @Override
        public HttpServer websocketHandler(Handler<ServerWebSocket> wsHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Handler<ServerWebSocket> websocketHandler() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer listen(int port) {
            return this;
        }

        @Override
        public HttpServer listen(int port, Handler<AsyncResult<HttpServer>> listenHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer listen(int port, String host) {
            return this;
        }

        @Override
        public HttpServer listen(int port, String host, Handler<AsyncResult<HttpServer>> listenHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close(Handler<AsyncResult<Void>> doneHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setClientAuthRequired(boolean required) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isClientAuthRequired() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setSSL(boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        @Override
        public boolean isSSL() {
            return ssl;
        }

        @Override
        public HttpServer setKeyStorePath(String path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getKeyStorePath() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setKeyStorePassword(String pwd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getKeyStorePassword() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setTrustStorePath(String path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getTrustStorePath() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setTrustStorePassword(String pwd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getTrustStorePassword() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setAcceptBacklog(int backlog) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getAcceptBacklog() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setTCPNoDelay(boolean tcpNoDelay) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setSendBufferSize(int size) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setReceiveBufferSize(int size) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setTCPKeepAlive(boolean keepAlive) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setReuseAddress(boolean reuse) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setSoLinger(int linger) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setTrafficClass(int trafficClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setUsePooledBuffers(boolean pooledBuffers) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isTCPNoDelay() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getSendBufferSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getReceiveBufferSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isTCPKeepAlive() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isReuseAddress() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getSoLinger() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getTrafficClass() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isUsePooledBuffers() {
            throw new UnsupportedOperationException();
        }
    }
}
