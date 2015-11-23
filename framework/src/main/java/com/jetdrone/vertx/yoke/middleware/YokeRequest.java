/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;

import com.jetdrone.vertx.yoke.util.Utils;
import io.vertx.core.http.*;
import io.vertx.core.net.SocketAddress;
import org.jetbrains.annotations.NotNull;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;

import com.jetdrone.vertx.yoke.core.Context;
import com.jetdrone.vertx.yoke.core.YokeCookie;
import com.jetdrone.vertx.yoke.core.YokeFileUpload;
import com.jetdrone.vertx.yoke.store.SessionStore;

/** YokeRequest is an extension to Vert.x *HttpServerRequest* with some helper methods to make it easier to perform common
 * tasks related to web application development.
 */
public class YokeRequest implements HttpServerRequest {

    private static final Comparator<String> ACCEPT_X_COMPARATOR = new Comparator<String>() {
        float getQuality(String s) {
            if (s == null) {
                return 0;
            }

            String[] params = s.split(" *; *");
            for (int i = 1; i < params.length; i++) {
                String[] q = params[1].split(" *= *");
                if ("q".equals(q[0])) {
                    return Float.parseFloat(q[1]);
                }
            }
            return 1;
        }
        @Override
        public int compare(String o1, String o2) {
            float f1 = getQuality(o1);
            float f2 = getQuality(o2);
            if (f1 < f2) {
                return 1;
            }
            if (f1 > f2) {
                return -1;
            }
            return 0;
        }
    };

    // the original request (if extensions need to access it, use the accessor)
    final private HttpServerRequest request;
    // the wrapped response (if extensions need to access it, use the accessor)
    final private YokeResponse response;
    // the request context
    final protected Context context;
    // is this request secure (if extensions need to access it, use the accessor)
    final private boolean secure = false;
    // session data store
    final protected SessionStore store;

    // we can overrride the setMethod
    private HttpMethod method;
    private long bodyLengthLimit = -1;
    // the body is protected so extensions can access the raw object instead of casted versions.
    protected Object body;
    private Map<String, YokeFileUpload> files;
    private Set<YokeCookie> cookies;
    // control flags
    private boolean expectMultiPartCalled = false;

    public YokeRequest(@NotNull final HttpServerRequest request, @NotNull final YokeResponse response, @NotNull final Context context, @NotNull final SessionStore store) {
        this.context = context;
        this.request = request;
        this.method = request.method();
        response.setMethod(this.method);
        this.response = response;
        this.store = store;
    }

    /** Allow getting properties in a generified way.
     *
     * @param name The key to get
     * @return {R} The found object
     */
    @SuppressWarnings("unchecked")
    public <R> R get(@NotNull final String name) {
        // do some conversions for JsonObject/JsonArray
        Object o = context.get(name);

        if (o instanceof Map) {
            return (R) new JsonObject((Map) o);
        }
        if (o instanceof List) {
            return (R) new JsonArray((List) o);
        }
        return (R) o;
    }

    /** Allow getting properties in a generified way and return defaultValue if the key does not exist.
     *
     * @param name The key to get
     * @param defaultValue value returned when the key does not exist
     * @return {R} The found object
     */
    public <R> R get(@NotNull final String name, R defaultValue) {
        if (context.containsKey(name)) {
            return get(name);
        } else {
            return defaultValue;
        }
    }

    /** Allows putting a value into the context
     *
     * @param name the key to store
     * @param value the value to store
     * @return {R} the previous value or null
     */
    @SuppressWarnings("unchecked")
    public <R> R put(@NotNull final String name, R value) {
        if (value == null) {
            return (R) context.remove(name);
        }
        return (R) context.put(name, value);
    }

    /** Allow getting headers in a generified way.
     *
     * @param name The key to get
     * @return The found object
     */
    @Override
    public String getHeader(@NotNull final String name) {
        return headers().get(name);
    }

    @Override
    public String getHeader(CharSequence charSequence) {
        return headers().get(charSequence);
    }

    /** Allow getting headers in a generified way.
     *
     * @param name The key to get
     * @return {List} The list of all found objects
     */
    public List<String> getAllHeaders(@NotNull final String name) {
        return headers().getAll(name);
    }

    /** Allow getting headers in a generified way and return defaultValue if the key does not exist.
     *
     * @param name The key to get
     * @param defaultValue value returned when the key does not exist
     * @return {String} The found object
     */
    public String getHeader(@NotNull final String name, String defaultValue) {
        if (headers().contains(name)) {
            return getHeader(name);
        } else {
            return defaultValue;
        }
    }

    /**
     * Access all request cookies
     * @return Set of cookies
     */
    public Set<YokeCookie> cookies() {
        return cookies;
    }

    /** Allow getting Cookie by name.
     *
     * @param name The key to get
     * @return The found object
     */
    public YokeCookie getCookie(@NotNull final String name) {
        if (cookies != null) {
            for (YokeCookie c : cookies) {
                if (name.equals(c.name())) {
                    return c;
                }
            }
        }
        return null;
    }

    /** Allow getting all Cookie by name.
     *
     * @param name The key to get
     * @return The found objects
     */
    public List<YokeCookie> getAllCookies(@NotNull final String name) {
        List<YokeCookie> foundCookies = new ArrayList<>();
        if (cookies != null) {
            for (YokeCookie c : cookies) {
                if (name.equals(c.name())) {
                    foundCookies.add(c);
                }
            }
        }
        return foundCookies;
    }

    // The original HTTP setMethod for the request. One of GET, PUT, POST, DELETE, TRACE, CONNECT, OPTIONS or HEAD
    public String originalMethod() {
        return request.method().name();
    }

    /** Package level mutator for the overrided setMethod
     * @param newMethod new setMethod GET, PUT, POST, DELETE, TRACE, CONNECT, OPTIONS or HEAD
     */
    void setMethod(@NotNull final HttpMethod newMethod) {
        this.method = newMethod;
        response.setMethod(this.method);
    }

    // Package level mutator for the bodyLength
    void setBodyLengthLimit(long limit) {
        bodyLengthLimit = limit;
    }

    // Holds the maximum allowed length for the setBody data. -1 for unlimited
    public long bodyLengthLimit() {
        return bodyLengthLimit;
    }

    /** Returns true if this request has setBody
     *
     * @return {Boolean} true if content-length or transfer-encoding is present
     */
    public boolean hasBody() {
        MultiMap headers = headers();
        return headers.contains("transfer-encoding") || headers.contains("content-length");
    }

    /** Returns the content length of this request setBody or -1 if header is not present.
     */
    public long contentLength() {
        String contentLengthHeader = headers().get("content-length");
        if (contentLengthHeader != null) {
            return Long.parseLong(contentLengthHeader);
        } else {
            return -1;
        }
    }

    /** The request body and eventually a parsed version of it in json or map */
    @SuppressWarnings("unchecked")
    public <V> V body() {
        if (body != null) {
            if (body instanceof Map) {
                return (V) new JsonObject((Map) body);
            }
            if (body instanceof List) {
                return (V) new JsonArray((List) body);
            }
        }

        return (V) body;
    }

    /** Mutator for the request setBody
     * The request setBody and eventually a parsed version of it in json or map*/
    void setBody(Object body) {
        this.body = body;
    }

    /** The uploaded setFiles */
    public Map<String, YokeFileUpload> files() {
        return files;
    }

    /** Get an uploaded file */
    public YokeFileUpload getFile(@NotNull final String name) {
        if (files == null) {
            return null;
        }

        return files.get(name);
    }

    /** The uploaded setFiles */
    void setFiles(Map<String, YokeFileUpload> files) {
        this.files = files;
    }

    /** Cookies */
    void setCookies(Set<YokeCookie> cookies) {
        this.cookies = cookies;
    }

    // Session management

    /** Destroys a session from the request context and also from the storage engine.
     */
    public void destroySession() {
    	JsonObject session = get("session");
        if (session == null) {
            return;
        }

        String sessionId = session.getString("id");
        // remove from the context
        put("session", null);

        if (sessionId == null) {
            return;
        }

        store.destroy(sessionId, new Handler<Object>() {
            @Override
            public void handle(Object error) {
                if (error != null) {
                    // TODO: better handling of errors
                    System.err.println(error);
                }
            }
        });
    }

    /** Loads a session given its session id and sets the "session" property in the request context.
     * @param sessionId the id to load
     * @param handler the success/complete handler
     */
    public void loadSession(final String sessionId, final Handler<Object> handler) {
        if (sessionId == null) {
            handler.handle(null);
            return;
        }

        store.get(sessionId, new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject session) {
                if (session != null) {
                    put("session", session);
                }

                response().headersHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {
                        int responseStatus = response().getStatusCode();
                        // Only save on success and redirect status codes
                        if (responseStatus >= 200 && responseStatus < 400) {
                        	JsonObject session = get("session");
                            if (session != null) {
                                store.set(sessionId, session, new Handler<Object>() {
                                    @Override
                                    public void handle(Object error) {
                                        if (error != null) {
                                            // TODO: better handling of errors
                                            System.err.println(error);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });

                handler.handle(null);
            }
        });
    }

    /** Create a new Session and store it with the underlying storage.
     * Internally create a entry in the request context under the name "session" and add a end handler to save that
     * object once the execution is terminated.
     *
     * @return {JsonObject} session
     */
    public JsonObject createSession() {
        final String sessionId = UUID.randomUUID().toString();
        return createSession(sessionId);
    }


    /** Create a new Session with custom Id and store it with the underlying storage.
     * Internally create a entry in the request context under the name "session" and add a end handler to save that
     * object once the execution is terminated. Custom session id could be used with external auth provider like mod-auth-mgr.
     *
     * @param sessionId custom session id
     * @return {JsonObject} session
     */
    public JsonObject createSession(@NotNull final String sessionId) {
        final JsonObject session = new JsonObject().put("id", sessionId);

        put("session", session);

        response().headersHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                int responseStatus = response().getStatusCode();
                // Only save on success and redirect status codes
                if (responseStatus >= 200 && responseStatus < 400) {
                	JsonObject session = get("session");
                    if (session != null) {
                        store.set(sessionId, session, new Handler<Object>() {
                            @Override
                            public void handle(Object error) {
                                if (error != null) {
                                    // TODO: better handling of errors
                                    System.err.println(error);
                                }
                            }
                        });
                    }
                }
            }
        });

        return session;
    }

    public boolean isSecure() {
        return secure;
    }

    private static String[] splitMime(@NotNull String mime) {
        // find any ; e.g.: "application/json;q=0.8"
        int space = mime.indexOf(';');

        if (space != -1) {
            mime = mime.substring(0, space);
        }

        String[] parts = mime.split("/");

        if (parts.length < 2) {
            return new String[] {
                    parts[0],
                    "*"
            };
        }

        return parts;
    }

    /** Check if the given type(s) is acceptable, returning the best match when true, otherwise null, in which
     * case you should respond with 406 "Not Acceptable".
     *
     * The type value must be a single mime type string such as "application/json" and is validated by checking
     * if the request string starts with it.
     */
    public String accepts(@NotNull final String... types) {
        String accept = getHeader("Accept");
        // accept anything when accept is not present
        if (accept == null) {
            return types[0];
        }

        // parse
        String[] acceptTypes = accept.split(" *, *");
        // sort on quality
        Arrays.sort(acceptTypes, ACCEPT_X_COMPARATOR);

        for (String senderAccept : acceptTypes) {
            String[] sAccept = splitMime(senderAccept);

            for (String appAccept : types) {
                String[] aAccept = splitMime(appAccept);

                if (
                        (sAccept[0].equals(aAccept[0]) || "*".equals(sAccept[0]) || "*".equals(aAccept[0])) &&
                        (sAccept[1].equals(aAccept[1]) || "*".equals(sAccept[1]) || "*".equals(aAccept[1]))) {
                    return senderAccept;
                }
            }
        }

        return null;
    }

    /** Returns the array of accept-? ordered by quality.
     */
    public List<String> sortedHeader(@NotNull final String header) {
        String accept = getHeader(header);
        // accept anything when accept is not present
        if (accept == null) {
            return Collections.emptyList();
        }

        // parse
        String[] items = accept.split(" *, *");
        // sort on quality
        Arrays.sort(items, ACCEPT_X_COMPARATOR);

        List<String> list = new ArrayList<>(items.length);

        for (String item : items) {
            // find any ; e.g.: "application/json;q=0.8"
            int space = item.indexOf(';');

            if (space != -1) {
                list.add(item.substring(0, space));
            } else {
                list.add(item);
            }
        }

        return list;
    }

    /** Check if the incoming request contains the "Content-Type"
     * header field, and it contains the give mime `type`.
     *
     * Examples:
     *
     * // With Content-Type: text/html; charset=utf-8
     * req.is('html');
     * req.is('text/html');
     * req.is('text/*');
     * // returns true
     *
     * // When Content-Type is application/json
     * req.is('json');
     * req.is('application/json');
     * req.is('application/*');
     * // returns true
     *
     * req.is('html');
     * // returns false
     *
     * @param type content type
     * @return true if content type is of type
     */
    public boolean is(@NotNull String type) {
        String ct = getHeader("Content-Type");
        if (ct == null) {
            return false;
        }
        // get the content type only (exclude charset)
        ct = ct.split(";")[0];

        // if we received an incomplete CT
        if (type.indexOf('/') == -1) {
            // when the content is incomplete we assume */type, e.g.:
            // json -> */json
            type = "*/" + type;
        }

        // process wildcards
        if (type.contains("*")) {
            String[] parts = type.split("/");
            String[] ctParts = ct.split("/");
            return "*".equals(parts[0]) && parts[1].equals(ctParts[1]) || "*".equals(parts[1]) && parts[0].equals(ctParts[0]);

        }

        return ct.contains(type);
    }

    /** Returns the ip address of the client, when trust-proxy is true (default) then first look into X-Forward-For
     * Header */
    public String ip() {
        Boolean trustProxy = (Boolean) context.get("trust-proxy");
        if (trustProxy != null && trustProxy) {
            String xForwardFor = getHeader("x-forward-for");
            if (xForwardFor != null) {
                String[] ips = xForwardFor.split(" *, *");
                if (ips.length > 0) {
                    return ips[0];
                }
            }
        }

        return request.remoteAddress().host();
    }

    /** Allow getting parameters in a generified way.
     *
     * @param name The key to get
     * @return {String} The found object
     */
    @Override
    public String getParam(@NotNull final String name) {
        return request.getParam(name);
    }

    /** Allow getting parameters in a generified way and return defaultValue if the key does not exist.
     *
     * @param name The key to get
     * @param defaultValue value returned when the key does not exist
     * @return {String} The found object
     */
    public String getParam(@NotNull final String name, String defaultValue) {
        String value = getParam(name);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    /** Allow getting parameters in a generified way.
     *
     * @param name The key to get
     * @return {List} The found object
     */
    public List<String> getParameterList(@NotNull final String name) {
        return params().getAll(name);
    }

    /** Allow getting form parameters in a generified way.
     *
     * @param name The key to get
     * @return {String} The found object
     */
    @Override
    public String getFormAttribute(@NotNull final String name) {
        return request.getFormAttribute(name);
    }

    @Override
    public ServerWebSocket upgrade() {
        return request.upgrade();
    }

    @Override
    public boolean isEnded() {
        return request.isEnded();
    }

    /** Allow getting form parameters in a generified way and return defaultValue if the key does not exist.
     *
     * @param name The key to get
     * @param defaultValue value returned when the key does not exist
     * @return {String} The found object
     */
    public String getFormAttribute(@NotNull final String name, String defaultValue) {
        String value = request.formAttributes().get(name);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    /** Allow getting form parameters in a generified way.
     *
     * @param name The key to get
     * @return {List} The found object
     */
    public List<String> getFormParameterList(@NotNull final String name) {
        return request.formAttributes().getAll(name);
    }

    /** Return the real request */
    public HttpServerRequest vertxHttpServerRequest() {
        return request;
    }

    /** Read the default locale for this request
     *
     * @return Locale (best match if more than one)
     */
    public Locale locale() {
        String languages = getHeader("Accept-Language");
        if (languages != null) {
            // parse
            String[] acceptLanguages = languages.split(" *, *");
            // sort on quality
            Arrays.sort(acceptLanguages, ACCEPT_X_COMPARATOR);

            String bestLanguage = acceptLanguages[0];

            int idx = bestLanguage.indexOf(';');

            if (idx != -1) {
                bestLanguage = bestLanguage.substring(0, idx).trim();
            }

            String[] parts = bestLanguage.split("_|-");
            switch (parts.length) {
                case 3: return new Locale(parts[0], parts[1], parts[2]);
                case 2: return new Locale(parts[0], parts[1]);
                case 1: return new Locale(parts[0]);
            }
        }

        return Locale.getDefault();
    }

    @Override
    public HttpVersion version() {
        return request.version();
    }

    @Override
    public HttpMethod method() {
        if (method != null) {
            return method;
        }
        return request.method();
    }

    @Override
    public String uri() {
        return request.uri();
    }

    @Override
    public String path() {
        return request.path();
    }

    private String cachedNormalizedPath = null;

    public String normalizedPath() {
        if (cachedNormalizedPath != null) {
            return cachedNormalizedPath;
        }

        String path = Utils.decodeURIComponent(request.path());

        // path should start with / so we should ignore it
        if (path.charAt(0) == '/') {
            path = path.substring(1);
        } else {
            return null;
        }

        String[] parts = path.split("/");
        Deque<String> resolved = new LinkedList<>();

        for (String p : parts) {
            if ("".equals(p)) {
                continue;
            }

            if (".".equals(p)) {
                continue;
            }

            if ("..".equals(p)) {
                // if there is no entry the path is trying to jump outside the root
                if (resolved.pollLast() == null) {
                    return null;
                }
                continue;
            }

            resolved.offerLast(p);
        }

        if (resolved.size() == 0) {
            cachedNormalizedPath = "/";
            return cachedNormalizedPath;
        }

        // re assemble the path
        StringBuilder sb = new StringBuilder();

        for (String s : resolved) {
            sb.append("/");
            sb.append(s);
        }

        cachedNormalizedPath = sb.toString();
        return cachedNormalizedPath;
    }

    @Override
    public String query() {
        return request.query();
    }

    @Override
    public YokeResponse response() {
        return response;
    }

    @Override
    public MultiMap headers() {
        return request.headers();
    }

    @Override
    public MultiMap params() {
        return request.params();
    }

    @Override
    public SocketAddress remoteAddress() {
        return request.remoteAddress();
    }

    @Override
    public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
        return request.peerCertificateChain();
    }

    @Override
    public String absoluteURI() {
        return request.absoluteURI();
    }

    @Override
    public YokeRequest bodyHandler(Handler<Buffer> bodyHandler) {
        request.bodyHandler(bodyHandler);
        return this;
    }

    @Override
    public NetSocket netSocket() {
        return request.netSocket();
    }

    @Override
    public YokeRequest setExpectMultipart(final boolean expect) {
        // if we expect
        if (expect) {
            // then only call it once
            if (!expectMultiPartCalled) {
                expectMultiPartCalled = true;
                request.setExpectMultipart(true);
            }
        } else {
            // if we don't expect reset even if we were called before
            expectMultiPartCalled = false;
            request.setExpectMultipart(false);
        }
        return this;
    }

    @Override
    public boolean isExpectMultipart() {
        return request.isExpectMultipart();
    }

    @Override
    public YokeRequest uploadHandler(Handler<HttpServerFileUpload> uploadHandler) {
        request.uploadHandler(uploadHandler);
        return this;
    }

    @Override
    public MultiMap formAttributes() {
        return request.formAttributes();
    }

    @Override
    public YokeRequest handler(Handler<Buffer> handler) {
        request.handler(handler);
        return this;
    }

    @Override
    public HttpServerRequest pause() {
        request.pause();
        return this;
    }

    @Override
    public YokeRequest resume() {
        request.resume();
        return this;
    }

    @Override
    public YokeRequest endHandler(Handler<Void> endHandler) {
        request.endHandler(endHandler);
        return this;
    }

    @Override
    public YokeRequest exceptionHandler(Handler<Throwable> handler) {
        request.exceptionHandler(handler);
        return this;
    }

    @Override
    public SocketAddress localAddress() {
        return request.localAddress();
    }
}