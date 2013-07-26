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

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.annotations.*;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Override
    public Middleware init(Vertx vertx, Logger logger) {
        super.init(vertx, logger);
        // since this call can happen after the bindings are in place we need to update all bindings to have a reference
        // to the vertx object
        for (PatternBinding binding : getBindings) {
            binding.middleware.init(vertx, logger);
        }

        for (PatternBinding binding : putBindings) {
            binding.middleware.init(vertx, logger);
        }

        for (PatternBinding binding : postBindings) {
            binding.middleware.init(vertx, logger);
        }

        for (PatternBinding binding : deleteBindings) {
            binding.middleware.init(vertx, logger);
        }

        for (PatternBinding binding : optionsBindings) {
            binding.middleware.init(vertx, logger);
        }

        for (PatternBinding binding : headBindings) {
            binding.middleware.init(vertx, logger);
        }

        for (PatternBinding binding : traceBindings) {
            binding.middleware.init(vertx, logger);
        }

        for (PatternBinding binding : connectBindings) {
            binding.middleware.init(vertx, logger);
        }

        for (PatternBinding binding : patchBindings) {
            binding.middleware.init(vertx, logger);
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
     * @param handler The middleware to call
     */
    public Router get(String pattern, Middleware handler) {
        addPattern(pattern, handler, getBindings);
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
     * @param handler The middleware to call
     */
    public Router put(String pattern, Middleware handler) {
        addPattern(pattern, handler, putBindings);
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
     * @param handler The middleware to call
     */
    public Router post(String pattern, Middleware handler) {
        addPattern(pattern, handler, postBindings);
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
     * @param handler The middleware to call
     */
    public Router delete(String pattern, Middleware handler) {
        addPattern(pattern, handler, deleteBindings);
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
     * @param handler The middleware to call
     */
    public Router options(String pattern, Middleware handler) {
        addPattern(pattern, handler, optionsBindings);
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
     * @param handler The middleware to call
     */
    public Router head(String pattern, Middleware handler) {
        addPattern(pattern, handler, headBindings);
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
     * @param handler The middleware to call
     */
    public Router trace(String pattern, Middleware handler) {
        addPattern(pattern, handler, traceBindings);
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
     * @param handler The middleware to call
     */
    public Router connect(String pattern, Middleware handler) {
        addPattern(pattern, handler, connectBindings);
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
     * @param handler The middleware to call
     */
    public Router patch(String pattern, Middleware handler) {
        addPattern(pattern, handler, patchBindings);
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
    public Router all(String pattern, Middleware handler) {
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
     * @param handler The middleware to call
     */
    public Router get(Pattern regex, Middleware handler) {
        addRegEx(regex, handler, getBindings);
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
     * @param handler The middleware to call
     */
    public Router put(Pattern regex, Middleware handler) {
        addRegEx(regex, handler, putBindings);
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
     * @param handler The middleware to call
     */
    public Router post(Pattern regex, Middleware handler) {
        addRegEx(regex, handler, postBindings);
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
     * @param handler The middleware to call
     */
    public Router delete(Pattern regex, Middleware handler) {
        addRegEx(regex, handler, deleteBindings);
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
     * @param handler The middleware to call
     */
    public Router options(Pattern regex, Middleware handler) {
        addRegEx(regex, handler, optionsBindings);
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
     * @param handler The middleware to call
     */
    public Router head(Pattern regex, Middleware handler) {
        addRegEx(regex, handler, headBindings);
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
     * @param handler The middleware to call
     */
    public Router trace(Pattern regex, Middleware handler) {
        addRegEx(regex, handler, traceBindings);
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
     * @param handler The middleware to call
     */
    public Router connect(Pattern regex, Middleware handler) {
        addRegEx(regex, handler, connectBindings);
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
     * @param handler The middleware to call
     */
    public Router patch(Pattern regex, Middleware handler) {
        addRegEx(regex, handler, patchBindings);
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
    public Router all(Pattern regex, Middleware handler) {
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
        String regex = sb.toString();
        PatternBinding binding = new PatternBinding(Pattern.compile(regex), groups, handler);
        // also pass the vertx object to the routes
        handler.init(vertx, logger);
        bindings.add(binding);
    }

    private void addRegEx(Pattern regex, Middleware handler, List<PatternBinding> bindings) {
        PatternBinding binding = new PatternBinding(regex, null, handler);
        // also pass the vertx object to the routes
        handler.init(vertx, logger);
        bindings.add(binding);
    }

    private void route(YokeRequest request, Handler<Object> next, List<PatternBinding> bindings) {
        for (PatternBinding binding: bindings) {
            Matcher m = binding.pattern.matcher(request.path());
            if (m.matches()) {
                Map<String, String> params = new HashMap<>(m.groupCount());
                if (binding.paramNames != null) {
                    // Named params
                    for (String param: binding.paramNames) {
                        params.put(param, m.group(param));
                    }
                } else {
                    // Un-named params
                    for (int i = 0; i < m.groupCount(); i++) {
                        params.put("param" + i, m.group(i + 1));
                    }
                }
                request.params().add(params);
                binding.middleware.handle(request, next);
                return;
            }
        }

        next.handle(null);
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

    private static String getPath(Object o, Method m) {
        // read the method one
        Path p = m.getAnnotation(Path.class);
        if (p != null) {
            // method path is present
            return p.value();
        }

        p = o.getClass().getAnnotation(Path.class);
        if (p != null) {
            // top level path is present
            return p.value();
        }
        throw new RuntimeException("Cannot infer the path for this method");
    }

    private static Middleware wrap(final Object o, final Method m, final boolean simple) {
        return new Middleware() {
            @Override
            public void handle(YokeRequest request, Handler<Object> next) {
                try {
                    if (simple) {
                        m.invoke(o, request);
                    } else {
                        m.invoke(o, request, next);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    next.handle(e);
                }
            }
        };
    }

    /**
     * Builds a Router from an annotated Java Object
     */
    public static Router from(Object... objs) {

        Router router = new Router();

        for (Object o : objs) {
            for (final Method m : o.getClass().getMethods()) {
                Annotation[] annotations = m.getAnnotations();
                // this method is not annotated
                if (annotations == null) {
                    continue;
                }

                Class[] paramTypes = m.getParameterTypes();
                int type = 0;

                if (paramTypes != null) {
                    if (paramTypes.length == 1 && paramTypes[0].equals(YokeRequest.class)) {
                        // single argument handler
                        type = 1;
                    }
                    if (paramTypes.length == 2 && paramTypes[0].equals(YokeRequest.class) && paramTypes[1].equals(Handler.class)) {
                        // double argument handler
                        type = 2;
                    }
                }

                if (type == 0) {
                    continue;
                }

                String path = getPath(o, m);

                for (Annotation a : annotations) {
                    if (a instanceof GET) {
                        router.get(path, wrap(o, m, type == 1));
                    }
                    if (a instanceof PUT) {
                        router.put(path, wrap(o, m, type == 1));
                    }
                    if (a instanceof POST) {
                        router.post(path, wrap(o, m, type == 1));
                    }
                    if (a instanceof DELETE) {
                        router.delete(path, wrap(o, m, type == 1));
                    }
                    if (a instanceof OPTIONS) {
                        router.options(path, wrap(o, m, type == 1));
                    }
                    if (a instanceof HEAD) {
                        router.head(path, wrap(o, m, type == 1));
                    }
                    if (a instanceof TRACE) {
                        router.trace(path, wrap(o, m, type == 1));
                    }
                    if (a instanceof PATCH) {
                        router.patch(path, wrap(o, m, type == 1));
                    }
                    if (a instanceof CONNECT) {
                        router.connect(path, wrap(o, m, type == 1));
                    }
                    if (a instanceof ALL) {
                        router.all(path, wrap(o, m, type == 1));
                    }
                }
            }
        }

        return router;
    }
}
