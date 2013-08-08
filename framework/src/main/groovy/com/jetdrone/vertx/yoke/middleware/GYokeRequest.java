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

import groovy.lang.Closure;
import groovy.json.JsonSlurper;
import org.vertx.groovy.core.http.HttpServerFileUpload;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpVersion;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetSocket;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class GYokeRequest extends YokeRequest /*implements org.vertx.groovy.core.http.HttpServerRequest*/ {

    private Map<String, GYokeFileUpload> files;

    public GYokeRequest(HttpServerRequest request, YokeResponse response, boolean secure, Map<String, Object> context) {
        super(request, response, secure, context);
    }

    private HttpServerFileUpload wrap(final org.vertx.java.core.http.HttpServerFileUpload jHttpServerFileUpload) {
        if (jHttpServerFileUpload == null) {
            return null;
        }

        return new HttpServerFileUpload() {
            @Override
            public HttpServerFileUpload streamToFileSystem(String s) {
                jHttpServerFileUpload.streamToFileSystem(s);
                return this;
            }

            @Override
            public String getFilename() {
                return jHttpServerFileUpload.filename();
            }

            @Override
            public String getName() {
                return jHttpServerFileUpload.name();
            }

            @Override
            public String getContentType() {
                return jHttpServerFileUpload.contentType();
            }

            @Override
            public String getContentTransferEncoding() {
                return jHttpServerFileUpload.contentTransferEncoding();
            }

            @Override
            public Charset getCharset() {
                return jHttpServerFileUpload.charset();
            }

            @Override
            public long getSize() {
                return jHttpServerFileUpload.size();
            }

            @Override
            public HttpServerFileUpload dataHandler(final Closure closure) {
                jHttpServerFileUpload.dataHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buffer) {
                        closure.call(buffer);
                    }
                });
                return this;
            }

            @Override
            public HttpServerFileUpload pause() {
                jHttpServerFileUpload.pause();
                return this;
            }

            @Override
            public HttpServerFileUpload resume() {
                jHttpServerFileUpload.resume();
                return this;
            }

            @Override
            public HttpServerFileUpload endHandler(final Closure closure) {
                jHttpServerFileUpload.endHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void v) {
                        closure.call(v);
                    }
                });
                return this;
            }

            @Override
            public HttpServerFileUpload exceptionHandler(final Closure closure) {
                jHttpServerFileUpload.exceptionHandler(new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable t) {
                        closure.call(t);
                    }
                });
                return this;
            }
        };
    }

    /**
     * The uploaded setFiles
     */
    public Map<String, GYokeFileUpload> getFiles() {
        if (files == null) {
            files = new HashMap<>();
            if (super.files() != null) {
                for (final Map.Entry<String, YokeFileUpload> entry : files().entrySet()) {
                    files.put(entry.getKey(), new GYokeFileUpload(entry.getValue()));
                }
            }
        }
        return files;
    }

    public GYokeFileUpload getFile(String name) {
        return getFiles().get(name);
    }

    public GYokeRequest uploadHandler(final Closure closure) {
        uploadHandler(new Handler<org.vertx.java.core.http.HttpServerFileUpload>() {
            @Override
            public void handle(org.vertx.java.core.http.HttpServerFileUpload event) {
                closure.call(wrap(event));
            }
        });
        return this;
    }

    public GYokeRequest bodyHandler(final Closure closure) {
        bodyHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer event) {
                closure.call(event);
            }
        });
        return this;
    }

    public GYokeRequest dataHandler(final Closure closure) {
        dataHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer event) {
                closure.call(event);
            }
        });
        return this;
    }

    public GYokeRequest endHandler(final Closure closure) {
        endHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                closure.call(event);
            }
        });
        return this;
    }

    public GYokeRequest exceptionHandler(final Closure closure) {
        exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable event) {
                closure.call(event);
            }
        });
        return this;
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

    public YokeResponse getResponse() {
        return response();
    }

    public MultiMap getHeaders() {
        return headers();
    }

    public MultiMap getParams() {
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
    
    public Object getJson() {
        Object _body = body();
        if (_body != null && _body instanceof String) {
            char initial = ((String)_body).charAt(0);
            if (initial == '{') {
                JsonSlurper slurper = new JsonSlurper();
                return slurper.parseText((String)_body);
            }
        }
        return null;
    }
    
    public Buffer getBufferBody() {
        return bufferBody();
    }

    public HttpServerRequest getVertxHttpServerRequest() {
        return vertxHttpServerRequest();
    }

    public HttpVersion getVersion() {
        return version();
    }

    public NetSocket getNetSocket() {
        return netSocket();
    }

    public MultiMap getFormAttributes() {
        return formAttributes();
    }

    public String getIp() {
        return ip();
    }
}
