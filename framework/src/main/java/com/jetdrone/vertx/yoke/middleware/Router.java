/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.annotations.*;
import com.jetdrone.vertx.yoke.util.AsyncIterator;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.Vertx;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
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

    @Override
    public Middleware init(Vertx vertx, String mount) {
        super.init(vertx, mount);
        // since this call can happen after the bindings are in place we need to update all bindings to have a reference
        // to the vertx object
        for (PatternBinding binding : getBindings) {
            binding.middleware.init(vertx, mount);
        }

        for (PatternBinding binding : putBindings) {
            binding.middleware.init(vertx, mount);
        }

        for (PatternBinding binding : postBindings) {
            binding.middleware.init(vertx, mount);
        }

        for (PatternBinding binding : deleteBindings) {
            binding.middleware.init(vertx, mount);
        }

        for (PatternBinding binding : optionsBindings) {
            binding.middleware.init(vertx, mount);
        }

        for (PatternBinding binding : headBindings) {
            binding.middleware.init(vertx, mount);
        }

        for (PatternBinding binding : traceBindings) {
            binding.middleware.init(vertx, mount);
        }

        for (PatternBinding binding : connectBindings) {
            binding.middleware.init(vertx, mount);
        }

        for (PatternBinding binding : patchBindings) {
            binding.middleware.init(vertx, mount);
        }

        return this;
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {

        switch (request.method()) {
            case "GET":
                route(request, next, getBindings);
                break;
            case "PUT":
                route(request, next, putBindings);
                break;
            case "POST":
                route(request, next, postBindings);
                break;
            case "DELETE":
                route(request, next, deleteBindings);
                break;
            case "OPTIONS":
                route(request, next, optionsBindings);
                break;
            case "HEAD":
                route(request, next, headBindings);
                break;
            case "TRACE":
                route(request, next, traceBindings);
                break;
            case "PATCH":
                route(request, next, patchBindings);
                break;
            case "CONNECT":
                route(request, next, connectBindings);
                break;
        }
    }

    /**
     * Specify a middleware that will be called for a matching HTTP GET
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router get(String pattern, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addPattern(pattern, handler, getBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP GET
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router get(String pattern, final Handler<YokeRequest> handler) {
        return get(pattern, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PUT
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router put(String pattern, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addPattern(pattern, handler, putBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PUT
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router put(String pattern, final Handler<YokeRequest> handler) {
        return put(pattern, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP POST
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router post(String pattern, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addPattern(pattern, handler, postBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP POST
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router post(String pattern, final Handler<YokeRequest> handler) {
        return post(pattern, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP DELETE
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router delete(String pattern, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addPattern(pattern, handler, deleteBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP DELETE
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router delete(String pattern, final Handler<YokeRequest> handler) {
        return delete(pattern, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP OPTIONS
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router options(String pattern, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addPattern(pattern, handler, optionsBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP OPTIONS
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router options(String pattern, final Handler<YokeRequest> handler) {
        return options(pattern, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP HEAD
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router head(String pattern, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addPattern(pattern, handler, headBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP HEAD
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router head(String pattern, final Handler<YokeRequest> handler) {
        return head(pattern, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP TRACE
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router trace(String pattern, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addPattern(pattern, handler, traceBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP TRACE
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router trace(String pattern, final Handler<YokeRequest> handler) {
        return trace(pattern, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP CONNECT
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router connect(String pattern, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addPattern(pattern, handler, connectBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP CONNECT
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router connect(String pattern, final Handler<YokeRequest> handler) {
        return connect(pattern, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PATCH
     * @param pattern The simple pattern
     * @param handlers The middleware to call
     */
    public Router patch(String pattern, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addPattern(pattern, handler, patchBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PATCH
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router patch(String pattern, final Handler<YokeRequest> handler) {
        return patch(pattern, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for all HTTP methods
     * @param pattern The simple pattern
     * @param handler The middleware to call
     */
    public Router all(String pattern, Middleware... handler) {
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
    public Router all(String pattern, final Handler<YokeRequest> handler) {
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
    public Router get(Pattern regex, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addRegEx(regex, handler, getBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP GET
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router get(Pattern regex, final Handler<YokeRequest> handler) {
        return get(regex, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PUT
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router put(Pattern regex, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addRegEx(regex, handler, putBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PUT
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router put(Pattern regex, final Handler<YokeRequest> handler) {
        return put(regex, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP POST
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router post(Pattern regex, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addRegEx(regex, handler, postBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP POST
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router post(Pattern regex, final Handler<YokeRequest> handler) {
        return post(regex, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP DELETE
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router delete(Pattern regex, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addRegEx(regex, handler, deleteBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP DELETE
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router delete(Pattern regex, final Handler<YokeRequest> handler) {
        return delete(regex, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP OPTIONS
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router options(Pattern regex, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addRegEx(regex, handler, optionsBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP OPTIONS
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router options(Pattern regex, final Handler<YokeRequest> handler) {
        return options(regex, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP HEAD
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router head(Pattern regex, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addRegEx(regex, handler, headBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP HEAD
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router head(Pattern regex, final Handler<YokeRequest> handler) {
        return head(regex, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP TRACE
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router trace(Pattern regex, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addRegEx(regex, handler, traceBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP TRACE
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router trace(Pattern regex, final Handler<YokeRequest> handler) {
        return trace(regex, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP CONNECT
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router connect(Pattern regex, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addRegEx(regex, handler, connectBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP CONNECT
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router connect(Pattern regex, final Handler<YokeRequest> handler) {
        return connect(regex, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PATCH
     * @param regex A regular expression
     * @param handlers The middleware to call
     */
    public Router patch(Pattern regex, Middleware... handlers) {
        for (Middleware handler : handlers) {
            addRegEx(regex, handler, patchBindings);
        }
        return this;
    }

    /**
     * Specify a middleware that will be called for a matching HTTP PATCH
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router patch(Pattern regex, final Handler<YokeRequest> handler) {
        return patch(regex, new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                handler.handle(request);
            }
        });
    }

    /**
     * Specify a middleware that will be called for all HTTP methods
     * @param regex A regular expression
     * @param handler The middleware to call
     */
    public Router all(Pattern regex, Middleware... handler) {
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
    public Router all(Pattern regex, final Handler<YokeRequest> handler) {
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

    public Router param(final String paramName, final Middleware handler) {
        // also pass the vertx object to the routes
        handler.init(vertx, mount);
        paramProcessors.put(paramName, handler);
        return this;
    }

    public Router param(final String paramName, final Pattern regex) {
        return param(paramName, new Middleware() {
            @Override
            public void handle(final YokeRequest request, final Handler<Object> next) {
                if (!regex.matcher(request.params().get(paramName)).matches()) {
                    // Bad Request
                    next.handle(400);
                    return;
                }

                next.handle(null);
            }
        });
    }

    private void addPattern(String input, Middleware handler, List<PatternBinding> bindings) {
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
        String regex = sb.toString();
        PatternBinding binding = new PatternBinding(Pattern.compile(regex), groups, handler);
        // also pass the vertx object to the routes
        handler.init(vertx, mount);
        bindings.add(binding);
    }

    private void addRegEx(Pattern regex, Middleware handler, List<PatternBinding> bindings) {
        PatternBinding binding = new PatternBinding(regex, null, handler);
        // also pass the vertx object to the routes
        handler.init(vertx, mount);
        bindings.add(binding);
    }

    private void route(final YokeRequest request, final Handler<Object> next, final List<PatternBinding> bindings) {

        new AsyncIterator<PatternBinding>(bindings) {
            @Override
            public void handle(PatternBinding binding) {
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
        if (m.matches()) {
            final MultiMap params = request.params();

            if (binding.paramNames != null) {
                // Named params
                new AsyncIterator<String>(binding.paramNames) {
                    @Override
                    public void handle(String param) {
                        if (hasNext()) {
                            params.add(param, m.group(param));
                            Middleware paramMiddleware = paramProcessors.get(param);
                            if (paramMiddleware != null) {
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
                            } else {
                                next();
                            }
                        } else {
                            binding.middleware.handle(request, next);
                        }
                    }
                };
            } else {
                // Un-named params
                for (int i = 0; i < m.groupCount(); i++) {
                    params.add("param" + i, m.group(i + 1));
                }
                binding.middleware.handle(request, next);
            }
        } else {
            next.handle(null);
        }
    }

    private static class PatternBinding {
        final Pattern pattern;
        final Middleware middleware;
        final Set<String> paramNames;

        private PatternBinding(Pattern pattern, Set<String> paramNames, Middleware middleware) {
            this.pattern = pattern;
            this.paramNames = paramNames;
            this.middleware = middleware;
        }
    }

    private static Middleware wrap(final Object o, final MethodHandle m, final String[] consumes, final String[] produces) {
        return new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                try {
                    // we only know how to process certain media types
                    if (consumes != null) {
                        boolean canConsume = false;
                        for (String c : consumes) {
                            if (request.is(c)) {
                                canConsume = true;
                                break;
                            }
                        }

                        if (!canConsume) {
                            // 415 Unsupported Media Type (we don't know how to handle this media)
                            next.handle(415);
                            return;
                        }
                    }

                    // the object was marked with a specific content type
                    if (produces != null) {
                        String bestContentType = request.accepts(produces);

                        // the client does not know how to handle our content type, return 406
                        if (bestContentType == null) {
                            next.handle(406);
                            return;
                        }

                        // mark the response with the correct content type (which allows middleware to know it later on)
                        request.response().setContentType(bestContentType);
                    }

                    m.invoke(o, request, next);

                } catch (Throwable e) {
                    next.handle(e);
                }
            }
        };
    }

    public static Router from(Object... objs) {
        final Router router = new Router();
        from(router, objs);

        return router;
    }

    /**
     * Builds a Router from an annotated Java Object
     */
    public static Router from(final Router router, Object... objs) {
        for (Object o : objs) {

            final Class c = o.getClass();

            Produces clazzProducesAnn = Processor.getAnnotation(c, Produces.class);
            Consumes clazzConsumesAnn = Processor.getAnnotation(c, Consumes.class);

            String[] clazzProduces = clazzProducesAnn != null ? clazzProducesAnn.value() : null;
            String[] clazzConsumes = clazzConsumesAnn != null ? clazzConsumesAnn.value() : null;

            for (final Method m : c.getMethods()) {
                Produces producesAnn = Processor.getAnnotation(m, Produces.class);
                Consumes consumesAnn = Processor.getAnnotation(m, Consumes.class);

                String[] produces = producesAnn != null ? producesAnn.value() : null;
                String[] consumes = consumesAnn != null ? consumesAnn.value() : null;

                if (produces == null) {
                    produces = clazzProduces;
                }
                if (consumes == null) {
                    consumes = clazzConsumes;
                }

                RegexParam regexParam = Processor.getAnnotation(m, RegexParam.class);

                // process the methods that have both YokeRequest and Handler

                if (Processor.isCompatible(m, ALL.class, YokeRequest.class, Handler.class)) {
                    if (regexParam != null) {
                        String name = regexParam.value();
                        String regex = regexParam.regex();

                        router.param(name, Pattern.compile(regex));
                    }

                    MethodHandle methodHandle = Processor.getMethodHandle(m, YokeRequest.class, Handler.class);
                    router.all(Processor.getAnnotation(m, ALL.class).value(), wrap(o, methodHandle, consumes, produces));
                }
                if (Processor.isCompatible(m, CONNECT.class, YokeRequest.class, Handler.class)) {
                    if (regexParam != null) {
                        String name = regexParam.value();
                        String regex = regexParam.regex();

                        router.param(name, Pattern.compile(regex));
                    }

                    MethodHandle methodHandle = Processor.getMethodHandle(m, YokeRequest.class, Handler.class);
                    router.connect(Processor.getAnnotation(m, CONNECT.class).value(), wrap(o, methodHandle, consumes, produces));
                }
                if (Processor.isCompatible(m, OPTIONS.class, YokeRequest.class, Handler.class)) {
                    if (regexParam != null) {
                        String name = regexParam.value();
                        String regex = regexParam.regex();

                        router.param(name, Pattern.compile(regex));
                    }

                    MethodHandle methodHandle = Processor.getMethodHandle(m, YokeRequest.class, Handler.class);
                    router.options(Processor.getAnnotation(m, OPTIONS.class).value(), wrap(o, methodHandle, consumes, produces));
                }
                if (Processor.isCompatible(m, HEAD.class, YokeRequest.class, Handler.class)) {
                    if (regexParam != null) {
                        String name = regexParam.value();
                        String regex = regexParam.regex();

                        router.param(name, Pattern.compile(regex));
                    }

                    MethodHandle methodHandle = Processor.getMethodHandle(m, YokeRequest.class, Handler.class);
                    router.head(Processor.getAnnotation(m, HEAD.class).value(), wrap(o, methodHandle, consumes, produces));
                }
                if (Processor.isCompatible(m, GET.class, YokeRequest.class, Handler.class)) {
                    if (regexParam != null) {
                        String name = regexParam.value();
                        String regex = regexParam.regex();

                        router.param(name, Pattern.compile(regex));
                    }

                    MethodHandle methodHandle = Processor.getMethodHandle(m, YokeRequest.class, Handler.class);
                    router.get(Processor.getAnnotation(m, GET.class).value(), wrap(o, methodHandle, consumes, produces));
                }
                if (Processor.isCompatible(m, POST.class, YokeRequest.class, Handler.class)) {
                    if (regexParam != null) {
                        String name = regexParam.value();
                        String regex = regexParam.regex();

                        router.param(name, Pattern.compile(regex));
                    }

                    MethodHandle methodHandle = Processor.getMethodHandle(m, YokeRequest.class, Handler.class);
                    router.post(Processor.getAnnotation(m, POST.class).value(), wrap(o, methodHandle, consumes, produces));
                }
                if (Processor.isCompatible(m, PUT.class, YokeRequest.class, Handler.class)) {
                    if (regexParam != null) {
                        String name = regexParam.value();
                        String regex = regexParam.regex();

                        router.param(name, Pattern.compile(regex));
                    }

                    MethodHandle methodHandle = Processor.getMethodHandle(m, YokeRequest.class, Handler.class);
                    router.put(Processor.getAnnotation(m, PUT.class).value(), wrap(o, methodHandle, consumes, produces));
                }
                if (Processor.isCompatible(m, PATCH.class, YokeRequest.class, Handler.class)) {
                    if (regexParam != null) {
                        String name = regexParam.value();
                        String regex = regexParam.regex();

                        router.param(name, Pattern.compile(regex));
                    }

                    MethodHandle methodHandle = Processor.getMethodHandle(m, YokeRequest.class, Handler.class);
                    router.patch(Processor.getAnnotation(m, PATCH.class).value(), wrap(o, methodHandle, consumes, produces));
                }
                if (Processor.isCompatible(m, DELETE.class, YokeRequest.class, Handler.class)) {
                    if (regexParam != null) {
                        String name = regexParam.value();
                        String regex = regexParam.regex();

                        router.param(name, Pattern.compile(regex));
                    }

                    MethodHandle methodHandle = Processor.getMethodHandle(m, YokeRequest.class, Handler.class);
                    router.delete(Processor.getAnnotation(m, DELETE.class).value(), wrap(o, methodHandle, consumes, produces));
                }

                if (Processor.isCompatible(m, Param.class, YokeRequest.class, Handler.class)) {
                    MethodHandle methodHandle = Processor.getMethodHandle(m, YokeRequest.class, Handler.class);
                    router.param(Processor.getAnnotation(m, Param.class).value(), wrap(o, methodHandle, null, null));
                }
            }
        }

        return router;
    }
}
