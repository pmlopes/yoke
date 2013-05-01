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
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class YokeTester extends Yoke {

    private final Vertx vertx;

    public YokeTester(Vertx vertx) {
        super(vertx);
        this.vertx = vertx;
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

            final Response response = new Response(vertx, handler);

            // start yoke
            handle(new HttpServerRequest() {

                MultiMap params = null;

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
                public HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
                    throw new UnsupportedOperationException("This mock does not support exceptionHandler");
                }
            });
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
