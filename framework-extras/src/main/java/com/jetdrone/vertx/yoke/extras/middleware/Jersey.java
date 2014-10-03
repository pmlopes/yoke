
package com.jetdrone.vertx.yoke.extras.middleware;

import static org.vertx.java.core.http.HttpHeaders.CONTENT_LENGTH;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.SecurityContext;

import com.jetdrone.vertx.yoke.AbstractMiddleware;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.ConfigHelper;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.glassfish.jersey.server.spi.RequestScopedInitializer;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpHeaders;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;

/**
 * <p>
 * A middleware that can forward requests to JAX-RS annotated resources.
 * </p>
 * <p>
 * <b>Use it as the last middleware and do not use it with a body consuming middleware!</b>
 * </p>
 * <p>
 * <b>Don't forget to add Jersey to your project's classpath!</b> For Maven, this would be done with
 * this addition to the POM:
 * </p>
 *
 * <pre>
 * {@code
 * <dependency>
 *   <groupId>org.glassfish.jersey.core</groupId>
 *   <artifactId>jersey-server</artifactId>
 *   <version>2.11</version>
 * </dependency>
 * }
 * </pre>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * {@code
 * yoke.use(new ErrorHandler(false))
 *     .use(new Timeout(10000L))
 *     .use(new Logger())
 *     .use(new Favicon())
 *     .use(new Router().get("/other/", r -> handleGetOther(r)))
 *     .use(new Jersey()
 *              .withPackages("com.acme.jaxrs.resources")
 *              .withInjectables(dependency1, dependency2));
 * }
 * </pre>
 * <p>
 * The context classes available in the JAX-RS resources are: {@link YokeRequest},
 * {@link YokeResponse}, {@link Vertx} and {@link org.vertx.java.platform.Container} (only if
 * provided with {@link #withVertxContainer(org.vertx.java.platform.Container)}).
 * </p>
 * <p>
 * Any object provided with {@link #withInjectables(Object...)} will be available via a standard
 * {@link javax.inject.Inject} annotation.
 * </p>
 * <p>
 * Implementation inspired by <a href=
 * "https://github.com/jersey/jersey/blob/master/containers/simple-http/src/main/java/org/glassfish/jersey/simple/SimpleContainer.java"
 * >Jersey's SimpleContainer</a> and <a href=
 * "https://github.com/englishtown/vertx-mod-jersey/blob/develop/vertx-mod-jersey/src/main/java/com/englishtown/vertx/jersey/impl/DefaultJerseyHandler.java"
 * >Englishtown's Jersey Mod</a>.
 * </p>
 */
public class Jersey extends AbstractMiddleware implements Container {

    private static final Logger LOGGER = LoggerFactory.getLogger(Jersey.class);

    private static class YokeOutputStream extends OutputStream {
        final YokeResponse response;
        Buffer buffer = new Buffer();
        boolean isClosed;

        private YokeOutputStream(final YokeResponse response) {
            this.response = response;
        }

        @Override
        public void write(final int b) throws IOException {
            checkState();
            buffer.appendByte((byte) b);
        }

        @Override
        public void write(final byte[] b) throws IOException {
            checkState();
            buffer.appendBytes(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            checkState();
            if (off == 0 && len == b.length) {
                buffer.appendBytes(b);
            } else {
                buffer.appendBytes(Arrays.copyOfRange(b, off, off + len));
            }
        }

        @Override
        public void flush() throws IOException {
            checkState();
            // Only flush to underlying very.x response if the content-length has been set
            if (buffer.length() > 0 && response.headers().contains(CONTENT_LENGTH)) {
                response.write(buffer);
                buffer = new Buffer();
            }
        }

        @Override
        public void close() throws IOException {
            // Write any remaining buffer to the vert.x response
            // Set content-length if not set yet
            if (buffer != null && buffer.length() > 0) {
                if (!response.headers().contains(HttpHeaders.CONTENT_LENGTH)) {
                    response.headers().add(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
                }
                response.write(buffer);
            }
            buffer = null;
            isClosed = true;
        }

        void checkState() {
            if (isClosed) {
                throw new IllegalStateException("Stream is closed");
            }
        }
    }

    private static class YokeChunkedOutputStream extends OutputStream {
        private final YokeResponse response;
        private boolean isClosed;

        private YokeChunkedOutputStream(final YokeResponse response) {
            this.response = response;
        }

        @Override
        public void write(final int b) throws IOException {
            checkState();
            final Buffer buffer = new Buffer();
            buffer.appendByte((byte) b);
            response.write(buffer);
        }

        @Override
        public void write(final byte[] b) throws IOException {
            checkState();
            response.write(new Buffer(b));
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            checkState();
            final Buffer buffer = new Buffer();
            if (off == 0 && len == b.length) {
                buffer.appendBytes(b);
            } else {
                buffer.appendBytes(Arrays.copyOfRange(b, off, off + len));
            }
            response.write(buffer);
        }

        @Override
        public void close() throws IOException {
            isClosed = true;
        }

        void checkState() {
            if (isClosed) {
                throw new IllegalStateException("Stream is closed");
            }
        }
    }

    private static class YokePrincipal implements Principal {
        private final String user;

        public YokePrincipal(final String user) {
            if (user == null) {
                throw new IllegalArgumentException("user can't be null");
            }

            this.user = user;
        }

        @Override
        public String getName() {
            return user;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + ":" + user;
        }

        @Override
        public int hashCode() {
            return 31 + ((user == null) ? 0 : user.hashCode());
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final YokePrincipal other = (YokePrincipal) obj;
            if (user == null) {
                if (other.user != null) return false;
            } else if (!user.equals(other.user)) return false;
            return true;
        }
    }

    private static final Type YOKE_REQUEST_TYPE = (new TypeLiteral<Ref<YokeRequest>>() {
    }).getType();

    private static final Type YOKE_RESPONSE_TYPE = (new TypeLiteral<Ref<YokeResponse>>() {
    }).getType();

    private static final Type VERTX_TYPE = (new TypeLiteral<Ref<Vertx>>() {
    }).getType();

    private static final Type CONTAINER_TYPE = (new TypeLiteral<Ref<org.vertx.java.platform.Container>>() {
    }).getType();

    private static class YokeRequestReferencingFactory extends ReferencingFactory<YokeRequest> {
        @Inject
        public YokeRequestReferencingFactory(final Provider<Ref<YokeRequest>> referenceFactory) {
            super(referenceFactory);
        }
    }

    private static class YokeResponseReferencingFactory extends ReferencingFactory<YokeResponse> {
        @Inject
        public YokeResponseReferencingFactory(final Provider<Ref<YokeResponse>> referenceFactory) {
            super(referenceFactory);
        }
    }

    private static class VertxReferencingFactory extends ReferencingFactory<Vertx> {
        @Inject
        public VertxReferencingFactory(final Provider<Ref<Vertx>> referenceFactory) {
            super(referenceFactory);
        }
    }

    private static class ContainerReferencingFactory extends
            ReferencingFactory<org.vertx.java.platform.Container> {
        @Inject
        public ContainerReferencingFactory(final Provider<Ref<org.vertx.java.platform.Container>> referenceFactory) {
            super(referenceFactory);
        }
    }

    private static class YokeBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bindFactory(YokeRequestReferencingFactory.class).to(YokeRequest.class)
                    .proxy(true)
                    .proxyForSameScope(false)
                    .in(RequestScoped.class);
            bindFactory(ReferencingFactory.<YokeRequest>referenceFactory()).to(
                    new TypeLiteral<Ref<YokeRequest>>() {
                    }).in(RequestScoped.class);
            bindFactory(YokeResponseReferencingFactory.class).to(YokeResponse.class)
                    .proxy(true)
                    .proxyForSameScope(false)
                    .in(RequestScoped.class);
            bindFactory(ReferencingFactory.<YokeResponse>referenceFactory()).to(
                    new TypeLiteral<Ref<YokeResponse>>() {
                    }).in(RequestScoped.class);
            bindFactory(VertxReferencingFactory.class).to(Vertx.class)
                    .proxy(true)
                    .proxyForSameScope(false)
                    .in(RequestScoped.class);
            bindFactory(ReferencingFactory.<Vertx>referenceFactory()).to(new TypeLiteral<Ref<Vertx>>() {
            }).in(RequestScoped.class);
            bindFactory(ContainerReferencingFactory.class).to(org.vertx.java.platform.Container.class)
                    .proxy(true)
                    .proxyForSameScope(false)
                    .in(RequestScoped.class);
            bindFactory(ReferencingFactory.<org.vertx.java.platform.Container>referenceFactory()).to(
                    new TypeLiteral<Ref<org.vertx.java.platform.Container>>() {
                    }).in(RequestScoped.class);
        }
    }

    private static final class YokeResponseWriter implements ContainerResponseWriter {
        private final YokeResponse response;
        private final Vertx vertx;

        private TimeoutHandler timeoutHandler;
        private long suspendTimerId;

        public YokeResponseWriter(final YokeResponse response, final Vertx vertx) {
            this.response = response;
            this.vertx = vertx;
            this.suspendTimerId = 0;
        }

        @Override
        public OutputStream writeResponseStatusAndHeaders(final long contentLength,
                                                          final ContainerResponse responseContext)
                throws ContainerException {
            response.setStatusCode(responseContext.getStatus());
            response.setStatusMessage(responseContext.getStatusInfo().getReasonPhrase());

            if (contentLength != -1) {
                response.putHeader(CONTENT_LENGTH, Long.toString(contentLength));
            }

            for (final Entry<String, List<String>> header : responseContext.getStringHeaders().entrySet()) {
                for (final String value : header.getValue()) {
                    response.putHeader(header.getKey(), value);
                }
            }

            if (responseContext.isChunked()) {
                response.setChunked(true);
                return new YokeChunkedOutputStream(response);
            } else {
                return new YokeOutputStream(response);
            }
        }

        @Override
        public void commit() {
            endResponse(response);
        }

        @Override
        public void failure(final Throwable t) {
            LOGGER.error(t.getMessage(), t);

            try {
                response.setStatusCode(500);
                response.setStatusMessage("Internal Server Error");
                response.end();
            } catch (final Exception e) {
                LOGGER.error("Failed to write failure response", e);
            }
        }

        @Override
        public boolean suspend(final long timeOut,
                               final TimeUnit timeUnit,
                               final TimeoutHandler timeoutHandler) {
            if (timeoutHandler == null) {
                throw new IllegalArgumentException("TimeoutHandler can't be null");
            }

            // if already suspended should return false according to documentation
            if (this.timeoutHandler != null) {
                return false;
            }

            this.timeoutHandler = timeoutHandler;

            doSuspend(timeOut, timeUnit);

            return true;
        }

        @Override
        public void setSuspendTimeout(final long timeOut, final TimeUnit timeUnit)
                throws IllegalStateException {
            if (timeoutHandler == null) {
                throw new IllegalStateException("Request not currently suspended");
            }

            if (suspendTimerId != 0) {
                vertx.cancelTimer(suspendTimerId);
            }

            doSuspend(timeOut, timeUnit);
        }

        private void doSuspend(final long timeOut, final TimeUnit timeUnit) {
            // if timeout <= 0, then it suspends indefinitely
            if (timeOut <= 0) {
                return;
            }

            final long ms = timeUnit.toMillis(timeOut);
            suspendTimerId = vertx.setTimer(ms, new Handler<Long>() {
                @Override
                public void handle(final Long $) {
                    YokeResponseWriter.this.timeoutHandler.onTimeout(YokeResponseWriter.this);
                }
            });
        }

        @Override
        public boolean enableResponseBuffering() {
            return false;
        }
    }

    private final ResourceConfig resourceConfig;

    private org.vertx.java.platform.Container vertxContainer;
    private ApplicationHandler applicationHandler;
    private ContainerLifecycleListener containerListener;

    public Jersey() {
        resourceConfig = new ResourceConfig();
    }

    public Jersey withVertxContainer(final org.vertx.java.platform.Container vertxContainer) {
        this.vertxContainer = vertxContainer;
        return this;
    }

    public Jersey withPackages(final String... packages) {
        resourceConfig.packages(packages);
        return this;
    }

    public Jersey withClasses(final Class<?>... classes) {
        resourceConfig.registerClasses(classes);
        return this;
    }

    public Jersey withInstances(final Object... instances) {
        resourceConfig.registerInstances(instances);
        return this;
    }

    public Jersey withName(final String name) {
        resourceConfig.setApplicationName(name);
        return this;
    }

    public Jersey withProperty(final String name, final Object value) {
        resourceConfig.property(name, value);
        return this;
    }

    public Jersey withInjectables(final Object... instances) {
        resourceConfig.register(new AbstractBinder() {
            @SuppressWarnings("unchecked")
            @Override
            protected void configure() {
                for (final Object instance : instances) {
                    bind(instance).to((Class<Object>) instance.getClass());
                }
            }
        });

        return this;
    }

    /**
     * Exposed in case some advanced feature is needed and not exposed by the <code>with...</code>
     * methods.
     */
    public ResourceConfig resourceConfig() {
        return resourceConfig;
    }

    @Override
    public Middleware init(final Yoke yoke, final String mount) {
        super.init(yoke, mount);
        initJersey();
        containerListener.onStartup(this);
        return this;
    }

    private void initJersey() {
        applicationHandler = new ApplicationHandler(resourceConfig, new YokeBinder());
        containerListener = ConfigHelper.getContainerLifecycleListener(applicationHandler);
    }

    @Override
    public ResourceConfig getConfiguration() {
        return resourceConfig;
    }

    @Override
    public ApplicationHandler getApplicationHandler() {
        return applicationHandler;
    }

    @Override
    public void reload() {
        reload(getConfiguration());
    }

    @Override
    public void reload(final ResourceConfig configuration) {
        containerListener.onShutdown(this);
        initJersey();
        containerListener.onReload(this);
        containerListener.onStartup(this);
    }

    @Override
    public void handle(final YokeRequest request, final Handler<Object> next) {
        final YokeResponse response = request.response();
        final YokeResponseWriter responseWriter = new YokeResponseWriter(response, vertx());
        final URI baseUri = getBaseUri(request);

        try {
            final ContainerRequest requestContext = new ContainerRequest(baseUri, request.absoluteURI(),
                    request.method(), getSecurityContext(request), new MapPropertiesDelegate());

            for (final String headerName : request.headers().names()) {
                requestContext.headers(headerName, request.headers().get(headerName));
            }
            requestContext.setWriter(responseWriter);
            requestContext.setRequestScopedInitializer(new RequestScopedInitializer() {
                @Override
                public void initialize(final ServiceLocator locator) {
                    locator.<Ref<YokeRequest>>getService(YOKE_REQUEST_TYPE).set(request);
                    locator.<Ref<YokeResponse>>getService(YOKE_RESPONSE_TYPE).set(response);
                    locator.<Ref<Vertx>>getService(VERTX_TYPE).set(vertx());
                    locator.<Ref<org.vertx.java.platform.Container>>getService(CONTAINER_TYPE).set(
                            vertxContainer);
                }
            });

            if (request.hasBody()) {
                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(final Buffer body) {
                        // TODO review this to handle large payloads gracefully
                        requestContext.setEntityStream(new ByteArrayInputStream(body.getBytes()));
                        applicationHandler.handle(requestContext);
                    }
                });
            } else {
                applicationHandler.handle(requestContext);
            }
        } catch (final Exception ex) {
            next.handle(ex);
        }
    }

    private static URI getBaseUri(final YokeRequest request) {
        try {
            final URI uri = request.absoluteURI();
            return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), "/", null, null);
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private static void endResponse(final YokeResponse response) {
        try {
            response.end();
        } catch (final Exception e) {
            LOGGER.error("Failed to commit response", e);
        }
    }

    private static SecurityContext getSecurityContext(final YokeRequest request) {
        return new SecurityContext() {
            @Override
            public boolean isUserInRole(final String role) {
                return false;
            }

            @Override
            public boolean isSecure() {
                return request.isSecure();
            }

            @Override
            public Principal getUserPrincipal() {
                // detect the user injected by the BasicAuth Yoke middleware
                final String user = request.get("user");
                return user == null ? null : new YokePrincipal(user);
            }

            @Override
            public String getAuthenticationScheme() {
                return null;
            }
        };
    }
}
