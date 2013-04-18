package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Router extends Middleware {

    private List<PatternBinding> getBindings = new ArrayList<>();
    private List<PatternBinding> putBindings = new ArrayList<>();
    private List<PatternBinding> postBindings = new ArrayList<>();
    private List<PatternBinding> deleteBindings = new ArrayList<>();
    private List<PatternBinding> optionsBindings = new ArrayList<>();
    private List<PatternBinding> headBindings = new ArrayList<>();
    private List<PatternBinding> traceBindings = new ArrayList<>();
    private List<PatternBinding> connectBindings = new ArrayList<>();
    private List<PatternBinding> patchBindings = new ArrayList<>();

    private Middleware noMatchHandler;

    @Override
    public void handle(HttpServerRequest request, Handler<Object> next) {

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
    public Router get(String pattern, final Handler<HttpServerRequest> handler) {
        return get(pattern, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router put(String pattern, final Handler<HttpServerRequest> handler) {
        return put(pattern, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router post(String pattern, final Handler<HttpServerRequest> handler) {
        return post(pattern, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router delete(String pattern, final Handler<HttpServerRequest> handler) {
        return delete(pattern, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router options(String pattern, final Handler<HttpServerRequest> handler) {
        return options(pattern, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router head(String pattern, final Handler<HttpServerRequest> handler) {
        return head(pattern, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router trace(String pattern, final Handler<HttpServerRequest> handler) {
        return trace(pattern, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router connect(String pattern, final Handler<HttpServerRequest> handler) {
        return connect(pattern, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router patch(String pattern, final Handler<HttpServerRequest> handler) {
        return patch(pattern, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router all(String pattern, final Handler<HttpServerRequest> handler) {
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
    public Router get(Pattern regex, final Handler<HttpServerRequest> handler) {
        return get(regex, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router put(Pattern regex, final Handler<HttpServerRequest> handler) {
        return put(regex, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router post(Pattern regex, final Handler<HttpServerRequest> handler) {
        return post(regex, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router delete(Pattern regex, final Handler<HttpServerRequest> handler) {
        return delete(regex, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router options(Pattern regex, final Handler<HttpServerRequest> handler) {
        return options(regex, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router head(Pattern regex, final Handler<HttpServerRequest> handler) {
        return head(regex, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router trace(Pattern regex, final Handler<HttpServerRequest> handler) {
        return trace(regex, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router connect(Pattern regex, final Handler<HttpServerRequest> handler) {
        return connect(regex, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router patch(Pattern regex, final Handler<HttpServerRequest> handler) {
        return patch(regex, new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
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
    public Router all(Pattern regex, final Handler<HttpServerRequest> handler) {
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
     * Specify a middleware that will be called when no other handlers match.
     * If this middleware is not specified default behaviour is to return a 404
     * @param handler
     */
    public Router noMatch(Middleware handler) {
        noMatchHandler = handler;
        return this;
    }

    /**
     * Specify a middleware that will be called when no other handlers match.
     * If this middleware is not specified default behaviour is to return a 404
     * @param handler
     */
    public Router noMatch(final Handler<HttpServerRequest> handler) {
        return noMatch(new Middleware() {
            @Override
            public void handle(HttpServerRequest request, Handler<Object> next) {
                handler.handle(request);
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
        String regex = sb.toString();
        PatternBinding binding = new PatternBinding(Pattern.compile(regex), groups, handler);
        bindings.add(binding);
    }

    private void addRegEx(Pattern regex, Middleware handler, List<PatternBinding> bindings) {
        PatternBinding binding = new PatternBinding(regex, null, handler);
        bindings.add(binding);
    }

    private void route(HttpServerRequest request, Handler<Object> next, List<PatternBinding> bindings) {
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
                request.params().putAll(params);
                binding.middleware.handle(request, next);
                return;
            }
        }
        if (noMatchHandler != null) {
            noMatchHandler.handle(request, next);
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
}
