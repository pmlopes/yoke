/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.IMiddleware;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.annotations.*;
import com.jetdrone.vertx.yoke.jmx.RouteMBean;
import com.jetdrone.vertx.yoke.util.AsyncIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * # Router
 *
 * Route request by path or regular expression. All *HTTP* verbs are available:
 *
 * * `GET`
 * * `PUT`
 * * `POST`
 * * `DELETE`
 * * `OPTIONS`
 * * `HEAD`
 * * `TRACE`
 * * `CONNECT`
 * * `PATCH`
 */
public class Router extends Middleware {

    private final List<PatternBinding> getBindings = new ArrayList<>();
    private final List<PatternBinding> putBindings = new ArrayList<>();
    private final List<PatternBinding> postBindings = new ArrayList<>();
    private final List<PatternBinding> deleteBindings = new ArrayList<>();
    private final List<PatternBinding> optionsBindings = new ArrayList<>();
    private final List<PatternBinding> headBindings = new ArrayList<>();
    private final List<PatternBinding> traceBindings = new ArrayList<>();
    private final List<PatternBinding> connectBindings = new ArrayList<>();
    private final List<PatternBinding> patchBindings = new ArrayList<>();

    private final Map<String, Middleware> paramProcessors = new HashMap<>();

    /**
     * Create a new Router Middleware.
     *
     * <pre>
     * new Router() {{
     *   get("/hello", new Handler&lt;YokeRequest&gt;() {
     *     public void handle(YokeRequest request) {
     *       request.response().end("Hello World!");
     *     }
     *   });
     * }}
     * </pre>
     */
    public Router() {
    }

    private void init(Yoke yoke, String mount, List<PatternBinding> bindings) {
        for (PatternBinding binding : bindings) {
            for (IMiddleware m : binding.middleware) {
                if (m instanceof Middleware) {
                    final Middleware middleware = (Middleware) m;
                    if (!middleware.isInitialized() && isInitialized()) {
                        middleware.init(yoke, mount);
                    }
                }
            }
        }
    }

    @Override
    public Middleware init(@NotNull final Yoke yoke, @NotNull final String mount) {
        super.init(yoke, mount);
        // since this call can happen after the bindings are in place we need to update all bindings to have a reference
        // to the vertx object
        init(yoke, mount, getBindings);
        init(yoke, mount, putBindings);
        init(yoke, mount, postBindings);
        init(yoke, mount, deleteBindings);
        init(yoke, mount, optionsBindings);
        init(yoke, mount, headBindings);
        init(yoke, mount, traceBindings);
        init(yoke, mount, connectBindings);

        for (String key : paramProcessors.keySet()) {
            final Middleware m = paramProcessors.get(key);
            if (!m.isInitialized()) {
                paramProcessors.get(key).init(yoke, mount);
            }
        }

        return this;
    }

    @Override
    public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {

        switch (request.method()) {
            case GET:
                route(request, next, getBindings);
                break;
            case PUT:
                route(request, next, putBindings);
                break;
            case POST:
                route(request, next, postBindings);
                break;
            case DELETE:
                route(request, next, deleteBindings);
                break;
            case OPTIONS:
                route(request, next, optionsBindings);
                break;
            case HEAD:
                route(request, next, headBindings);
                break;
            case TRACE:
                route(request, next, traceBindings);
                break;
            case PATCH:
                route(request, next, patchBindings);
                break;
            case CONNECT:
                route(request, next, connectBindings);
                break;
        }
    }

    /**
     * Specify a middleware that will be called for a matching HTTP GET
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router get(@NotNull final String pattern, @NotNull final IMiddleware... handlers) {
        addPattern("GET", pattern, handlers, getBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP GET
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router get(@NotNull final String pattern, @NotNull final Handler<YokeRequest> handler) {
        return get(pattern, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PUT
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router put(@NotNull final String pattern, @NotNull final IMiddleware... handlers) {
        addPattern("PUT", pattern, handlers, putBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PUT
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router put(@NotNull final String pattern, @NotNull final Handler<YokeRequest> handler) {
        return put(pattern, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP POST
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router post(@NotNull final String pattern, @NotNull final IMiddleware... handlers) {
        addPattern("POST", pattern, handlers, postBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP POST
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router post(@NotNull final String pattern, @NotNull final Handler<YokeRequest> handler) {
        return post(pattern, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP DELETE
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router delete(@NotNull final String pattern, @NotNull final IMiddleware... handlers) {
        addPattern("DELETE", pattern, handlers, deleteBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP DELETE
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router delete(@NotNull final String pattern, @NotNull final Handler<YokeRequest> handler) {
        return delete(pattern, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP OPTIONS
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router options(@NotNull final String pattern, @NotNull final IMiddleware... handlers) {
        addPattern("OPTIONS", pattern, handlers, optionsBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP OPTIONS
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router options(@NotNull final String pattern, @NotNull final Handler<YokeRequest> handler) {
        return options(pattern, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP HEAD
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router head(@NotNull final String pattern, @NotNull final IMiddleware... handlers) {
        addPattern("HEAD", pattern, handlers, headBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP HEAD
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router head(@NotNull final String pattern, @NotNull final Handler<YokeRequest> handler) {
        return head(pattern, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP TRACE
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router trace(@NotNull final String pattern, @NotNull final IMiddleware... handlers) {
        addPattern("TRACE", pattern, handlers, traceBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP TRACE
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router trace(@NotNull final String pattern, @NotNull final Handler<YokeRequest> handler) {
        return trace(pattern, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP CONNECT
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router connect(@NotNull final String pattern, @NotNull final IMiddleware... handlers) {
        addPattern("CONNECT", pattern, handlers, connectBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP CONNECT
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router connect(@NotNull final String pattern, @NotNull final Handler<YokeRequest> handler) {
        return connect(pattern, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PATCH
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router patch(@NotNull final String pattern, @NotNull final IMiddleware... handlers) {
        addPattern("PATCH", pattern, handlers, patchBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PATCH
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router patch(@NotNull final String pattern, @NotNull final Handler<YokeRequest> handler) {
        return patch(pattern, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for all HTTP methods
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router all(@NotNull final String pattern, @NotNull final Middleware... handler) {
        get(pattern, handler);
        put(pattern, handler);
        post(pattern, handler);
        delete(pattern, handler);
        options(pattern, handler);
        head(pattern, handler);
        trace(pattern, handler);
        connect(pattern, handler);
        patch(pattern, handler);
        return this;
    }

    /**
     * Specify a middleware that will be called for all HTTP methods
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router all(@NotNull final String pattern, @NotNull final Handler<YokeRequest> handler) {
        get(pattern, handler);
        put(pattern, handler);
        post(pattern, handler);
        delete(pattern, handler);
        options(pattern, handler);
        head(pattern, handler);
        trace(pattern, handler);
        connect(pattern, handler);
        patch(pattern, handler);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP GET
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router get(@NotNull final Pattern regex, @NotNull final IMiddleware... handlers) {
        addRegEx("GET", regex, handlers, getBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP GET
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router get(@NotNull final Pattern regex, @NotNull final Handler<YokeRequest> handler) {
        return get(regex, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PUT
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router put(@NotNull final Pattern regex, @NotNull final IMiddleware... handlers) {
        addRegEx("PUT", regex, handlers, putBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PUT
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router put(@NotNull final Pattern regex, @NotNull final Handler<YokeRequest> handler) {
        return put(regex, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP POST
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router post(@NotNull final Pattern regex, @NotNull final IMiddleware... handlers) {
        addRegEx("POST", regex, handlers, postBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP POST
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router post(@NotNull final Pattern regex, @NotNull final Handler<YokeRequest> handler) {
        return post(regex, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP DELETE
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router delete(@NotNull final Pattern regex, @NotNull final IMiddleware... handlers) {
        addRegEx("DELETE", regex, handlers, deleteBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP DELETE
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router delete(@NotNull final Pattern regex, @NotNull final Handler<YokeRequest> handler) {
        return delete(regex, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP OPTIONS
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router options(@NotNull final Pattern regex, @NotNull final IMiddleware... handlers) {
        addRegEx("OPTIONS", regex, handlers, optionsBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP OPTIONS
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router options(@NotNull final Pattern regex, @NotNull final Handler<YokeRequest> handler) {
        return options(regex, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP HEAD
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router head(@NotNull final Pattern regex, @NotNull final IMiddleware... handlers) {
        addRegEx("HEAD", regex, handlers, headBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP HEAD
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router head(@NotNull final Pattern regex, @NotNull final Handler<YokeRequest> handler) {
        return head(regex, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP TRACE
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router trace(@NotNull final Pattern regex, @NotNull final IMiddleware... handlers) {
        addRegEx("TRACE", regex, handlers, traceBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP TRACE
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router trace(@NotNull final Pattern regex, @NotNull final Handler<YokeRequest> handler) {
        return trace(regex, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP CONNECT
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router connect(@NotNull final Pattern regex, @NotNull final IMiddleware... handlers) {
        addRegEx("CONNECT", regex, handlers, connectBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP CONNECT
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router connect(@NotNull final Pattern regex, @NotNull final Handler<YokeRequest> handler) {
        return connect(regex, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PATCH
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router patch(@NotNull final Pattern regex, @NotNull final IMiddleware... handlers) {
        addRegEx("PATCH", regex, handlers, patchBindings);
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PATCH
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router patch(@NotNull final Pattern regex, @NotNull final Handler<YokeRequest> handler) {
        return patch(regex, new Middleware() {
            @Override
            public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for all HTTP methods
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router all(@NotNull final Pattern regex, @NotNull final Middleware... handler) {
        get(regex, handler);
        put(regex, handler);
        post(regex, handler);
        delete(regex, handler);
        options(regex, handler);
        head(regex, handler);
        trace(regex, handler);
        connect(regex, handler);
        patch(regex, handler);
        return this;
    }

    /**
     * Specify a middleware that will be called for all HTTP methods
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router all(@NotNull final Pattern regex, @NotNull final Handler<YokeRequest> handler) {
        get(regex, handler);
        put(regex, handler);
        post(regex, handler);
        delete(regex, handler);
        options(regex, handler);
        head(regex, handler);
        trace(regex, handler);
        connect(regex, handler);
        patch(regex, handler);
        return this;
    }

    public Router param(@NotNull final String paramName, @NotNull final Middleware handler) {
        // also pass the vertx object to the routes
        if (!handler.isInitialized() && isInitialized()) {
            handler.init(yoke, mount);
        }
        paramProcessors.put(paramName, handler);
        return this;
    }

    public Router param(@NotNull final String paramName, @NotNull final Pattern regex) {
        return param(paramName, new Middleware() {
            @Override
            public void handle(@NotNull final YokeRequest request, @NotNull final Handler<Object> next) {
                if (!regex.matcher(request.params().get(paramName)).matches()) {
                    // Bad Request
                    next.handle(400);
                    return;
                }

                next.handle(null);
            }
        });
    }

    private void addPattern(String verb, String input, IMiddleware[] handler, List<PatternBinding> bindings) {
        // We need to search for any :<token name> tokens in the String and replace them with named capture groups
        Matcher m =  Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)").matcher(input);
        StringBuffer sb = new StringBuffer();
        Set<String> groups = new HashSet<>();
        while (m.find()) {
            String group = m.group().substring(1);
            if (groups.contains(group)) {
                throw new IllegalArgumentException("Cannot use identifier " + group + " more than once in pattern string");
            }
            m.appendReplacement(sb, "(?<$1>[^\\/]+)");
            groups.add(group);
        }
        m.appendTail(sb);
        // ignore tailing slash if not part of the input, not really REST but common on other frameworks
        if (sb.charAt(sb.length() - 1) != '/') {
            sb.append("\\/?$");
        }

        Pattern regex = Pattern.compile(sb.toString());
        boolean exists = false;
        // verify if the binding already exists, if yes add to it
        for (PatternBinding pb : bindings) {
            if (pb.isFor(input)) {
                exists = true;
                pb.addMiddleware(handler);
                break;
            }
        }

        if (!exists) {
            PatternBinding binding = new PatternBinding(hashCode(), verb, input, regex, groups, handler);
            bindings.add(binding);
        }

        // also pass the vertx object to the routes
        for (IMiddleware h : handler) {
            if (h instanceof Middleware) {
                final Middleware middleware = (Middleware) h;
                if (!middleware.isInitialized() && isInitialized()) {
                    middleware.init(yoke, mount);
                }
            }
        }
    }

    private void addRegEx(String verb, Pattern regex, IMiddleware handler[], List<PatternBinding> bindings) {
        boolean exists = false;
        // verify if the binding already exists, if yes add to it
        for (PatternBinding pb : bindings) {
            if (pb.isFor(regex)) {
                pb.addMiddleware(handler);
                exists = true;
                break;
            }
        }

        if (!exists) {
            PatternBinding binding = new PatternBinding(hashCode(), verb, null, regex, null, handler);
            bindings.add(binding);
        }

        // also pass the vertx object to the routes
        for (IMiddleware h : handler) {
            if (h instanceof Middleware) {
                final Middleware middleware = (Middleware) h;
                if (!middleware.isInitialized() && isInitialized()) {
                    middleware.init(yoke, mount);
                }
            }
        }
    }

    private void route(final YokeRequest request, final Handler<Object> next, final List<PatternBinding> bindings) {

        new AsyncIterator<PatternBinding>(bindings) {
            @Override
            public void handle(final PatternBinding binding) {
                if (hasNext()) {
                    route(request, binding, new Handler<Object>() {
                        @Override
                        public void handle(Object err) {
                            if (err == null) {
                                next();
                            } else {
                                next.handle(err);
                            }
                        }
                    });
                } else {
                    // continue with yoke
                    next.handle(null);
                }
            }
        };
    }

    private void route(final YokeRequest request, final PatternBinding binding, final Handler<Object> next) {
        final Matcher m = binding.pattern.matcher(request.path());
        final Vertx vertx = vertx();
        
        if (m.matches()) {
            final MultiMap params = request.params();

            if (binding.paramNames != null) {
                // Named params
                new AsyncIterator<String>(binding.paramNames) {
                    @Override
                    public void handle(String param) {
                        if (hasNext()) {
                            params.set(param, m.group(param));
                            final Middleware paramMiddleware = paramProcessors.get(param);
                            if (paramMiddleware != null) {
                                // do not block main loop
                                vertx.runOnContext(new Handler<Void>() {
                                    @Override
                                    public void handle(Void event) {
                                        paramMiddleware.handle(request, new Handler<Object>() {
                                            @Override
                                            public void handle(Object err) {
                                                if (err == null) {
                                                    next();
                                                } else {
                                                    next.handle(err);
                                                }
                                            }
                                        });
                                    }
                                });
                            } else {
                                next();
                            }
                        } else {
                            // middlewares
                            new AsyncIterator<IMiddleware>(binding.middleware) {
                                @Override
                                public void handle(final IMiddleware middleware) {
                                    if (hasNext()) {
                                        // do not block main loop
                                        vertx.runOnContext(new Handler<Void>() {
                                            @Override
                                            public void handle(Void event) {
                                                middleware.handle(request, new Handler<Object>() {
                                                    @Override
                                                    public void handle(Object err) {
                                                        if (err == null) {
                                                            next();
                                                        } else {
                                                            next.handle(err);
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        next.handle(null);
                                    }
                                }
                            };
                        }
                    }
                };
            } else {
                // Un-named params
                for (int i = 0; i < m.groupCount(); i++) {
                    params.set("param" + i, m.group(i + 1));
                }

                // middlewares
                new AsyncIterator<IMiddleware>(binding.middleware) {
                    @Override
                    public void handle(final IMiddleware middleware) {
                        if (hasNext()) {
                            // do not block main loop
                            vertx.runOnContext(new Handler<Void>() {
                                @Override
                                public void handle(Void event) {
                                    middleware.handle(request, new Handler<Object>() {
                                        @Override
                                        public void handle(Object err) {
                                            if (err == null) {
                                                next();
                                            } else {
                                                next.handle(err);
                                            }
                                        }
                                    });
                                }
                            });
                        } else {
                            next.handle(null);
                        }
                    }
                };
            }
        } else {
            next.handle(null);
        }
    }

    private static class PatternBinding {

        //Get the MBean server
        private static final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        private final Pattern pattern;
        private final String route;

        private final List<IMiddleware> middleware = new ArrayList<>();
        private final Set<String> paramNames;

        private final ObjectName objectName;

        private PatternBinding(int hasCode, @NotNull String verb, @Nullable String route, @NotNull Pattern pattern, @Nullable Set<String> paramNames, @NotNull IMiddleware[] middleware) {
            this.route = route;
            this.pattern = pattern;
            this.paramNames = paramNames;
            Collections.addAll(this.middleware, middleware);

            // register on JMX
            try {
                String jmxName = route;

                // fallback to decode from the pattern
                if (jmxName == null) {
                    jmxName = pattern.pattern();
                }
                objectName = new ObjectName("com.jetdrone.yoke:type=Route@" + hasCode + ",method=" + verb + ",path=" + ObjectName.quote(jmxName));
            } catch (MalformedObjectNameException e) {
                throw new RuntimeException(e);
            }

            try {
                mbs.registerMBean(new RouteMBean(this.middleware), objectName);
            } catch (InstanceAlreadyExistsException e) {
                // ignore
            } catch (MBeanRegistrationException | NotCompliantMBeanException e) {
                throw new RuntimeException(e);
            }
        }

        private void addMiddleware(@NotNull IMiddleware[] middleware) {
            Collections.addAll(this.middleware, middleware);

            // un register if present
            try {
                mbs.unregisterMBean(objectName);
            } catch (InstanceNotFoundException e) {
                // ignore
            } catch (MBeanRegistrationException e) {
                throw new RuntimeException(e);
            }

            // re register if present
            try {
                mbs.registerMBean(new RouteMBean(this.middleware), objectName);
            } catch (InstanceAlreadyExistsException e) {
                // ignore
            } catch (MBeanRegistrationException | NotCompliantMBeanException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean isFor(@NotNull String route) {
            return this.route != null && this.route.equals(route);
        }

        private boolean isFor(@NotNull Pattern regex) {
            return this.route == null && this.pattern.pattern().equals(regex.pattern());
        }
    }

    public static Router from(@NotNull Object... objs) {
        final Router router = new Router();
        from(router, objs);

        return router;
    }

    /**
     * Builds a Router from an annotated Java Object
     */
    public static Router from(@NotNull final Router router, @NotNull Object... objs) {
        for (Object o : objs) {
            Processor.process(router, o);
        }

        return router;
    }
}
