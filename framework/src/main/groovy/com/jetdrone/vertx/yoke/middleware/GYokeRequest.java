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

import com.jetdrone.vertx.yoke.store.SessionStore;
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

    /**
     * Allow getting properties in a generified way.
     *
     * @param name The key to get
     * @param <R> The type of the return
     * @return The found object
     */
    @SuppressWarnings("unchecked")
    public <R> R getAt(String name) {
        return get(name);
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
    public <R> R putAt(String name, R value) {
        return put(name, value);
    }

    private Map<String, GYokeFileUpload> files;

    public GYokeRequest(HttpServerRequest request, YokeResponse response, boolean secure, Map<String, Object> context, SessionStore store) {
        super(request, response, secure, context, store);
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

    public GYokeRequest setExpectMultiPart(boolean expect) {
        expectMultiPart(expect);
        return this;
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
            Boolean validObject = (Boolean)get("valid_json_object");
            Boolean validArray = (Boolean)get("valid_json_array");
            if (validObject != null && validObject) {
                return parseText((String)_body);
            }
            if (validArray != null && validArray) {
                return parseText((String)_body);
            }
        }
        return null;
    }

    private Object parseText(String content) {
        JsonSlurper slurper = new JsonSlurper();
        return slurper.parseText(content);
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

    public InetSocketAddress getLocalAddress() {
        return localAddress();
    }

    public String getNormalizedPath() {
        return normalizedPath();
    }

    public String getSessionId() {
        return sessionId();
    }

    public void loadSessionData(final Closure handler) {
        loadSessionData(new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject sessionData) {
                if (sessionData == null) {
                    handler.call();
                    return;
                }
                handler.call(sessionData.toMap());
            }
        });
    }

    public void saveSessionData(Map<String, Object> sessionData, final Closure handler) {
        saveSessionData(new JsonObject(sessionData), new Handler<String>() {
            @Override
            public void handle(String status) {
                handler.call(status);
            }
        });
    }

    public void destroySession(final Closure handler) {
        destroySession(new Handler<String>() {
            @Override
            public void handle(String status) {
                handler.call(status);
            }
        });
    }
}
