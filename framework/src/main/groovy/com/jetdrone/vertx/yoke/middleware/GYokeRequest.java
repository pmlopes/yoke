/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.core.Context;
import com.jetdrone.vertx.yoke.core.GMultiMap;
import com.jetdrone.vertx.yoke.store.SessionStore;
import groovy.lang.Closure;
import org.vertx.groovy.core.http.HttpServerFileUpload;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpVersion;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetSocket;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class GYokeRequest extends YokeRequest implements org.vertx.groovy.core.http.HttpServerRequest {

    /**
     * Allow getting properties in a generified way.
     *
     * @param name The key to get
     * @param <R> The type of the return
     * @return The found object
     */
    @SuppressWarnings("unchecked")
    public <R> R getAt(String name) {
        // do some conversions for JsonObject/JsonArray
        Object o = context.get(name);

        if (o instanceof JsonObject) {
            return (R) ((JsonObject) o).toMap();
        }
        if (o instanceof JsonArray) {
            return (R) ((JsonArray) o).toList();
        }
        return (R) o;
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
    // Groovy helpers
    private GMultiMap params;
    private GMultiMap formAttributes;
    private GMultiMap headers;

    private final GYokeResponse response;

    public GYokeRequest(HttpServerRequest request, GYokeResponse response, boolean secure, Context context, SessionStore store) {
        super(request, response, secure, context, store);
        this.response = response;
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

    public Locale getLocale() {
        return locale();
    }

    public GYokeResponse getResponse() {
        return response;
    }

    public GMultiMap getHeaders() {
        if (headers == null) {
            headers = new GMultiMap(headers());
        }
        return headers;
    }

    public GMultiMap getParams() {
        if (params == null) {
            params = new GMultiMap(params());
        }
        return params;
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
        return body;
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

    @Override
    public GYokeRequest resume() {
        super.resume();
        return this;
    }

    @Override
    public GYokeRequest pause() {
        super.pause();
        return this;
    }

    public GMultiMap getFormAttributes() {
        if (formAttributes == null) {
            formAttributes = new GMultiMap(formAttributes());
        }
        return formAttributes;
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

    public void loadSession(String sessionId, final Closure handler) {
        loadSession(sessionId, new Handler<Object>() {
            @Override
            public void handle(Object event) {
                handler.call(event);
            }
        });
    }

    /**
     * Access all request cookies
     * @return Set of cookies
     */
    public Set<YokeCookie> getCookies() {
        return cookies();
    }

}
