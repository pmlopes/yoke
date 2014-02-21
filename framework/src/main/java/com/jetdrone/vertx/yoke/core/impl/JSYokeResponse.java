package com.jetdrone.vertx.yoke.core.impl;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.core.Context;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;
import io.netty.handler.codec.http.Cookie;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrappedException;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.streams.ReadStream;

import java.util.Map;

import static com.jetdrone.vertx.yoke.core.impl.JSUtil.*;

// TODO: verify all return types since all functions return undefined

final class JSYokeResponse  extends YokeResponse implements Scriptable {

    public JSYokeResponse(HttpServerResponse response, Context context, Map<String, Engine> engines) {
        super(response, context, engines);
    }

    private Scriptable prototype, parent;

    // cacheable scriptable/callable objects

    private Callable addCookie;
    private Callable close;
    private Callable closeHandler;
    private Callable drainHandler;
    private Callable end;
    private Callable endHandler;
    private Callable exceptionHandler;
    private Callable getHeader;
    private Scriptable headers;
    private Callable headersHandler;
    private Callable jsonp;
    private Callable putHeader;
    private Callable putTrailer;
    private Callable redirect;
    private Callable render;
    private Callable sendFile;
    private Callable setContentType;
    private Callable setWriteQueueMaxSize;
    private Scriptable trailers;
    private Callable write;

    @Override
    public String getClassName() {
        return "JSYokeResponse";
    }

    @Override
    public Object get(String name, Scriptable start) {
        switch (name) {
            case "addCookie":
                if (addCookie == null) {
                    addCookie = new Callable() {
                        @Override
                        public Object call(org.mozilla.javascript.Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, Cookie.class)) {
                                JSYokeResponse.this.addCookie((Cookie) args[0]);
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return addCookie;
            case "close":
                if (close == null) {
                    close = new Callable() {
                        @Override
                        public Object call(org.mozilla.javascript.Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }
                            JSYokeResponse.this.close();
                            return Undefined.instance;
                        }
                    };
                }
                return close;
            case "closeHandler":
                if (closeHandler == null) {
                    closeHandler = new Callable() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public Object call(final org.mozilla.javascript.Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, Handler.class)) {
                                JSYokeResponse.this.closeHandler((Handler) args[0]);
                                return Undefined.instance;
                            }

                            if (is(args, Callable.class)) {
                                JSYokeResponse.this.closeHandler(new Handler<Void>() {
                                    @Override
                                    public void handle(Void event) {
                                        ((Callable) args[0]).call(cx, scope, thisObj, EMPTY_OBJECT_ARRAY);
                                    }
                                });
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return closeHandler;
            case "drainHandler":
                if (drainHandler == null) {
                    drainHandler = new Callable() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public Object call(final org.mozilla.javascript.Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, Handler.class)) {
                                JSYokeResponse.this.drainHandler((Handler) args[0]);
                                return Undefined.instance;
                            }

                            if (is(args, Callable.class)) {
                                JSYokeResponse.this.drainHandler(new Handler<Void>() {
                                    @Override
                                    public void handle(Void event) {
                                        ((Callable) args[0]).call(cx, scope, thisObj, EMPTY_OBJECT_ARRAY);
                                    }
                                });
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return drainHandler;
            case "end":
                if (end == null) {
                    end = new Callable() {
                        @Override
                        public Object call(org.mozilla.javascript.Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, String.class, String.class)) {
                                JSYokeResponse.this.end((String) args[0], (String) args[1]);
                                return Undefined.instance;
                            }

                            if (is(args, JsonElement.class)) {
                                JSYokeResponse.this.end((JsonElement) args[0]);
                                return Undefined.instance;
                            }

                            if (is(args, ReadStream.class)) {
                                JSYokeResponse.this.end((ReadStream) args[0]);
                                return Undefined.instance;
                            }

                            if (is(args, String.class)) {
                                JSYokeResponse.this.end((String) args[0]);
                                return Undefined.instance;
                            }

                            if (is(args, Buffer.class)) {
                                JSYokeResponse.this.end((Buffer) args[0]);
                                return Undefined.instance;
                            }

                            if (is(args)) {
                                JSYokeResponse.this.end();
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return end;
            case "endHandler":
                if (endHandler == null) {
                    endHandler = new Callable() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public Object call(final org.mozilla.javascript.Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, Handler.class)) {
                                JSYokeResponse.this.endHandler((Handler) args[0]);
                                return Undefined.instance;
                            }

                            if (is(args, Callable.class)) {
                                JSYokeResponse.this.endHandler(new Handler<Void>() {
                                    @Override
                                    public void handle(Void event) {
                                        ((Callable) args[0]).call(cx, scope, thisObj, EMPTY_OBJECT_ARRAY);
                                    }
                                });
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return endHandler;
            case "exceptionHandler":
                if (exceptionHandler == null) {
                    exceptionHandler = new Callable() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public Object call(final org.mozilla.javascript.Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, Handler.class)) {
                                JSYokeResponse.this.exceptionHandler((Handler) args[0]);
                                return Undefined.instance;
                            }

                            if (is(args, Callable.class)) {
                                JSYokeResponse.this.exceptionHandler(new Handler<Throwable>() {
                                    @Override
                                    public void handle(Throwable throwable) {
                                        ((Callable) args[0]).call(cx, scope, thisObj, new Object[] {
                                                new WrappedException(throwable)});
                                    }
                                });
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return exceptionHandler;
            case "getHeader":
                if (getHeader == null) {
                    getHeader = new Callable() {
                        @Override
                        public Object call(org.mozilla.javascript.Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, String.class, String.class)) {
                                JSYokeResponse.this.getHeader((String) args[0], (String) args[1]);
                                return Undefined.instance;
                            }

                            if (is(args, String.class)) {
                                JSYokeResponse.this.getHeader((String) args[0]);
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return getHeader;
            case "statusCode":
                return getStatusCode();
            case "statusMessage":
                return getStatusMessage();
            case "headers":
                if (headers == null) {
                    headers = toScriptable(headers());
                }
                return headers;
            case "headersHandler":
                if (headersHandler == null) {
                    headersHandler = new Callable() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public Object call(final org.mozilla.javascript.Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, Handler.class)) {
                                JSYokeResponse.this.headersHandler((Handler) args[0]);
                                return Undefined.instance;
                            }

                            if (is(args, Callable.class)) {
                                JSYokeResponse.this.headersHandler(new Handler<Void>() {
                                    @Override
                                    public void handle(Void event) {
                                        ((Callable) args[0]).call(cx, scope, thisObj, EMPTY_OBJECT_ARRAY);
                                    }
                                });
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return headersHandler;
            case "chunked":
                return isChunked();
            case "jsonp":
                if (jsonp == null) {
                    jsonp = new Callable() {
                        @Override
                        public Object call(org.mozilla.javascript.Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, String.class, JsonElement.class)) {
                                JSYokeResponse.this.jsonp((String) args[0], (JsonElement) args[1]);
                                return Undefined.instance;
                            }
                            if (is(args, String.class, String.class)) {
                                JSYokeResponse.this.jsonp((String) args[0], (String) args[1]);
                                return Undefined.instance;
                            }
                            if (is(args, JsonElement.class)) {
                                JSYokeResponse.this.jsonp((JsonElement) args[0]);
                                return Undefined.instance;
                            }
                            if (is(args, String.class)) {
                                JSYokeResponse.this.jsonp((String) args[0]);
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return jsonp;
            case "putHeader":
                if (putHeader == null) {
                    putHeader = new Callable() {
                        @Override
                        public Object call(org.mozilla.javascript.Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, CharSequence.class, CharSequence.class)) {
                                JSYokeResponse.this.putHeader((CharSequence) args[0], (CharSequence) args[1]);
                                return Undefined.instance;
                            }

                            if (is(args, CharSequence.class, Iterable.class)) {
                                JSYokeResponse.this.putHeader((CharSequence) args[0], (Iterable) args[1]);
                                return Undefined.instance;
                            }

                            if (is(args, String.class, String.class)) {
                                JSYokeResponse.this.putHeader((String) args[0], (String) args[1]);
                                return Undefined.instance;
                            }

                            if (is(args, String.class, Iterable.class)) {
                                JSYokeResponse.this.putHeader((String) args[0], (Iterable<String>) args[1]);
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return putHeader;
            case "putTrailer":
                if (putTrailer == null) {
                    putTrailer = new Callable() {
                        @Override
                        public Object call(org.mozilla.javascript.Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, CharSequence.class, CharSequence.class)) {
                                JSYokeResponse.this.putTrailer((CharSequence) args[0], (CharSequence) args[1]);
                                return Undefined.instance;
                            }

                            if (is(args, CharSequence.class, Iterable.class)) {
                                JSYokeResponse.this.putTrailer((CharSequence) args[0], (Iterable) args[1]);
                                return Undefined.instance;
                            }

                            if (is(args, String.class, String.class)) {
                                JSYokeResponse.this.putTrailer((String) args[0], (String) args[1]);
                                return Undefined.instance;
                            }

                            if (is(args, String.class, Iterable.class)) {
                                JSYokeResponse.this.putTrailer((String) args[0], (Iterable<String>) args[1]);
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return putTrailer;
            case "redirect":
                if (redirect == null) {
                    redirect = new Callable() {
                        @Override
                        public Object call(org.mozilla.javascript.Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, Integer.class, String.class)) {
                                JSYokeResponse.this.redirect((Integer) args[0], (String) args[1]);
                                return Undefined.instance;
                            }

                            if (is(args, String.class)) {
                                JSYokeResponse.this.redirect((String) args[0]);
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return redirect;
            case "render":
                if (render == null) {
                    render = new Callable() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public Object call(final org.mozilla.javascript.Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, String.class, Handler.class)) {
                            	JSYokeResponse.this.render((String) args[0], (Handler) args[1]);
                                return Undefined.instance;
                            }
                            
                            if (is(args, String.class, String.class, Handler.class)) {
                            	JSYokeResponse.this.render((String) args[0], (String) args[1], (Handler) args[2]);
                                return Undefined.instance;
                            }
                            

                            if (is(args, String.class, Callable.class)) {
                            	
                                JSYokeResponse.this.render((String) args[0], new Handler<Object>() {
                                    @Override
                                    public void handle(Object error) {
                                        ((Callable) args[1]).call(cx, scope, thisObj, new Object[]{error});
                                    }
                                });                            	                            	                           	
                                return Undefined.instance;
                            }
                            
                            if (is(args, String.class, String.class, Callable.class)) {
                                JSYokeResponse.this.render((String) args[0], (String) args[1], new Handler<Object>() {
                                    @Override
                                    public void handle(Object error) {
                                        ((Callable) args[2]).call(cx, scope, thisObj, new Object[]{error});
                                    }
                                }); 
                                return Undefined.instance;
                            }                            

                            if (is(args, String.class)) {
                                JSYokeResponse.this.render((String) args[0]);
                                return Undefined.instance;
                            }
                            
                            if (is(args, String.class, String.class)) {
                                JSYokeResponse.this.render((String) args[0], (String) args[1]);
                                return Undefined.instance;
                            }                            

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return render;
            case "sendFile":
                if (sendFile == null) {
                    sendFile = new Callable() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public Object call(final org.mozilla.javascript.Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, String.class, String.class, Handler.class)) {
                                JSYokeResponse.this.sendFile((String) args[0], (String) args[1], (Handler) args[2]);
                                return Undefined.instance;
                            }

                            if (is(args, String.class, String.class, Callable.class)) {
                                JSYokeResponse.this.sendFile((String) args[0], (String) args[1], new Handler<AsyncResult<Void>>() {
                                    @Override
                                    public void handle(AsyncResult<Void> result) {
                                        ((Callable) args[2]).call(cx, scope, thisObj, new Object[]{result.cause(), result.result()});
                                    }
                                });
                                return Undefined.instance;
                            }

                            if (is(args, String.class, String.class)) {
                                JSYokeResponse.this.sendFile((String) args[0], (String) args[1]);
                                return Undefined.instance;
                            }

                            if (is(args, String.class, Handler.class)) {
                                JSYokeResponse.this.sendFile((String) args[0], (Handler) args[1]);
                                return Undefined.instance;
                            }

                            if (is(args, String.class, Callable.class)) {
                                JSYokeResponse.this.sendFile((String) args[0], new Handler<AsyncResult<Void>>() {
                                    @Override
                                    public void handle(AsyncResult<Void> result) {
                                        ((Callable) args[1]).call(cx, scope, thisObj, new Object[]{result.cause(), result.result()});
                                    }
                                });
                                return Undefined.instance;
                            }

                            if (is(args, String.class)) {
                                JSYokeResponse.this.sendFile((String) args[0]);
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return sendFile;
            case "setContentType":
                if (setContentType == null) {
                    setContentType = new Callable() {
                        @Override
                        public Object call(org.mozilla.javascript.Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, String.class, String.class)) {
                                JSYokeResponse.this.setContentType((String) args[0], (String) args[1]);
                                return Undefined.instance;
                            }

                            if (is(args, String.class)) {
                                JSYokeResponse.this.setContentType((String) args[0]);
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return setContentType;
            case "setWriteQueueMaxSize":
                if (setWriteQueueMaxSize == null) {
                    setWriteQueueMaxSize = new Callable() {
                        @Override
                        public Object call(org.mozilla.javascript.Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, Integer.class)) {
                                JSYokeResponse.this.setWriteQueueMaxSize((Integer) args[0]);
                                return Undefined.instance;
                            }

                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return setWriteQueueMaxSize;
            case "trailers":
                if (trailers == null) {
                    trailers = toScriptable(trailers());
                }
                return trailers;
            case "write":
                if (write == null) {
                    write = new Callable() {
                        @Override
                        public Object call(org.mozilla.javascript.Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            if (JSYokeResponse.this != thisObj) {
                                throw new RuntimeException("[native JSYokeFunction not bind to JSYokeResponse]");
                            }

                            if (is(args, String.class, String.class)) {
                                JSYokeResponse.this.write((String) args[0], (String) args[1]);
                                return Undefined.instance;
                            }

                            if (is(args, String.class)) {
                                JSYokeResponse.this.write((String) args[0]);
                                return Undefined.instance;
                            }

                            if (is(args, Buffer.class)) {
                                JSYokeResponse.this.write((Buffer) args[0]);
                                return Undefined.instance;
                            }
                            
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return write;
            case "writeQueueFull":
                return writeQueueFull();
            default:
                // fail to find
                return NOT_FOUND;
        }
    }

    @Override
    public Object get(int index, Scriptable start) {
        return NOT_FOUND;
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return false;
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return false;
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        switch (name) {
            case "chunked":
                setChunked((Boolean) value);
                return;
            case "statusCode":
                setStatusCode((Integer) value);
                return;
            case "statusMessage":
                setStatusMessage((String) value);
                return;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        put(Integer.toString(index), start, value);
    }

    @Override
    public void delete(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(int index) {
        delete(Integer.toString(index));
    }

    @Override
    public Scriptable getPrototype() {
        return prototype;
    }

    @Override
    public void setPrototype(Scriptable prototype) {
        this.prototype = prototype;
    }

    @Override
    public Scriptable getParentScope() {
        return parent;
    }

    @Override
    public void setParentScope(Scriptable parent) {
        this.parent = parent;
    }

    @Override
    public Object[] getIds() {
        return EMPTY_OBJECT_ARRAY;
    }

    @Override
    public Object getDefaultValue(Class<?> hint) {
        return "[object JSYokeResponse]";
    }

    @Override
    public boolean hasInstance(Scriptable instance) {
        return instance != null && instance instanceof JSYokeResponse;
    }
}
