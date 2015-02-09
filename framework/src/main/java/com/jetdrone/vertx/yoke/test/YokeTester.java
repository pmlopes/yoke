/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.test;

import com.jetdrone.vertx.yoke.Yoke;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.vertx.java.core.*;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.*;
import org.vertx.java.core.net.NetSocket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * # YokeTester
 */
public class YokeTester {

    private final Vertx vertx;
    private final HttpServer fakeServer = new FakeHttpServer();

    public YokeTester(Yoke yoke, boolean fakeSSL) {
        this.vertx = yoke.vertx();
        fakeServer.setSSL(fakeSSL);
        yoke.listen(fakeServer);
    }

    public YokeTester(Yoke yoke) {
        this(yoke, false);
    }

    public void request(final String method, final String url, final Handler<Response> handler) {
        request(method, url, new CaseInsensitiveMultiMap(), new Buffer(0), handler);
    }

    public void request(final String method, final String url, final MultiMap headers, final Handler<Response> handler) {
        request(method, url, headers, new Buffer(0), handler);
    }

    public void request(final String method, final String url, final MultiMap headers, final Buffer body, final Handler<Response> handler) {
        try {
            final URI uri = new URI(url);
            final boolean urlEncoded = "application/x-www-form-urlencoded".equalsIgnoreCase(headers.get("content-type"));

            final Response response = new Response(vertx, handler);

            // start yoke
            vertx.runOnContext(new Handler<Void>() {
                @Override
                public void handle(Void aVoid) {
                    fakeServer.requestHandler().handle(new HttpServerRequest() {

                        MultiMap params = null;
                        MultiMap attributes = null;
                        final Random random = new Random();

                        final NetSocket netSocket = new NetSocket() {
                            @Override
                            public String writeHandlerID() {
                                throw new UnsupportedOperationException("This mock does not support netSocket::writeHandlerID");
                            }

                            @Override
                            public NetSocket write(Buffer data) {
                                throw new UnsupportedOperationException("This mock does not support netSocket::write");
                            }

                            @Override
                            public NetSocket write(String str) {
                                throw new UnsupportedOperationException("This mock does not support netSocket::write");
                            }

                            @Override
                            public NetSocket write(String str, String enc) {
                                throw new UnsupportedOperationException("This mock does not support netSocket::write");
                            }

                            @Override
                            public NetSocket sendFile(String filename) {
                                throw new UnsupportedOperationException("This mock does not support netSocket::sendFile");
                            }

                            @Override
                            public NetSocket sendFile(String filename, Handler<AsyncResult<Void>> resultHandler) {
                                throw new UnsupportedOperationException("This mock does not support netSocket::sendFile");
                            }

                            @Override
                            public InetSocketAddress remoteAddress() {
                                return new InetSocketAddress("localhost", random.nextInt(Short.MAX_VALUE));
                            }

                            @Override
                            public InetSocketAddress localAddress() {
                                return new InetSocketAddress("localhost", random.nextInt(Short.MAX_VALUE));
                            }

                            @Override
                            public void close() {
                                throw new UnsupportedOperationException("This mock does not support netSocket::close");
                            }

                            @Override
                            public NetSocket closeHandler(Handler<Void> handler) {
                                throw new UnsupportedOperationException("This mock does not support netSocket::closeHandler");
                            }

                            @Override
                            public NetSocket ssl(Handler<Void> handler) {
                                throw new UnsupportedOperationException("This mock does not support netSocket::ssl");
                            }

                            @Override
                            public boolean isSsl() {
                                return fakeServer.isSSL();
                            }

                            @Override
                            public NetSocket setWriteQueueMaxSize(int maxSize) {
                                throw new UnsupportedOperationException("This mock does not support netSocket::setWriteQueueMaxSize");
                            }

                            @Override
                            public boolean writeQueueFull() {
                                return false;
                            }

                            @Override
                            public NetSocket drainHandler(Handler<Void> handler) {
                                throw new UnsupportedOperationException("This mock does not support netSocket::drainHandler");
                            }

                            @Override
                            public NetSocket endHandler(Handler<Void> endHandler) {
                                throw new UnsupportedOperationException("This mock does not support netSocket::endHandler");
                            }

                            @Override
                            public NetSocket dataHandler(Handler<Buffer> handler) {
                                throw new UnsupportedOperationException("This mock does not support netSocket::dataHandler");
                            }

                            @Override
                            public NetSocket pause() {
                                throw new UnsupportedOperationException("This mock does not support netSocket::pause");
                            }

                            @Override
                            public NetSocket resume() {
                                throw new UnsupportedOperationException("This mock does not support netSocket::resume");
                            }

                            @Override
                            public NetSocket exceptionHandler(Handler<Throwable> handler) {
                                throw new UnsupportedOperationException("This mock does not support netSocket::exceptionHandler");
                            }
                        };

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
                            return uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "") + (uri.getFragment() != null ? "#" + uri.getFragment() : "");
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
                                    for (Map.Entry<String, List<String>> entry : prms.entrySet()) {
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
                        public InetSocketAddress localAddress() {
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
                        public HttpServerRequest bodyHandler(final Handler<Buffer> bodyHandler) {
                            if (bodyHandler != null) {
                                vertx.runOnContext(new Handler<Void>() {
                                    @Override
                                    public void handle(Void event) {
                                        bodyHandler.handle(body);
                                    }
                                });
                            }
                            return this;
                        }

                        @Override
                        public HttpServerRequest dataHandler(final Handler<Buffer> handler) {
                            if (handler != null) {
                                vertx.runOnContext(new Handler<Void>() {
                                    @Override
                                    public void handle(Void event) {
                                        handler.handle(body);
                                    }
                                });
                            }
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
                        public HttpServerRequest endHandler(final Handler<Void> endHandler) {
                            if (endHandler != null) {
                                vertx.runOnContext(new Handler<Void>() {
                                    @Override
                                    public void handle(Void event) {
                                        endHandler.handle(null);
                                    }
                                });
                            }
                            return this;
                        }

                        @Override
                        public NetSocket netSocket() {
                            return netSocket;
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
                                        for (Map.Entry<String, List<String>> entry : prms.entrySet()) {
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
        public HttpServer setCompressionSupported(boolean compressionSupported) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCompressionSupported() {
            return false;
        }

        @Override
        public HttpServer setMaxWebSocketFrameSize(int maxSize) {
            return this;
        }

        @Override
        public int getMaxWebSocketFrameSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpServer setWebSocketSubProtocols(String... subProtocols) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> getWebSocketSubProtocols() {
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

        @Override
        public HttpServer setSSLContext(SSLContext sslContext) {
            throw new UnsupportedOperationException();
        }
    }
}
