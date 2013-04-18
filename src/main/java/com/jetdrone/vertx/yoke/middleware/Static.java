package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.MimeType;
import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.file.FileProps;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.http.HttpServerRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class Static extends Middleware {

    static final SimpleDateFormat ISODATE;
    private String directoryTemplate;

    static {
        ISODATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS zzz");
        ISODATE.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final String root;
    private final long maxAge;

    public Static(String root, long maxAge) {
        this.root = root;
        this.maxAge = maxAge;
    }

    public Static(String root) {
        this(root, 86400000);
    }

    @Override
    public void setVertx(Vertx vertx) {
        try {
            super.setVertx(vertx);
            Buffer buf = vertx.fileSystem().readFileSync(getClass().getResource("directory.html").getPath());
            directoryTemplate = buf.toString("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeHeaders(final HttpServerRequest request, final FileProps props) {

        Map<String, Object> headers = request.response().headers();

        if (!headers.containsKey("etag")) {
            headers.put("etag", "\"" + props.size() + "-" + props.lastModifiedTime().getTime() + "\"");
        }

        if (!headers.containsKey("date")) {
            headers.put("date", ISODATE.format(new Date()));
        }

        if (!headers.containsKey("cache-control")) {
            headers.put("cache-control", "public, max-age=" + maxAge / 1000);
        }

        if (!headers.containsKey("last-modified")) {
            headers.put("last-modified", ISODATE.format(props.lastModifiedTime()));
        }
    }

    private void sendFile(final HttpServerRequest request, final String file, final FileProps props) {
        // write content type
        String contentType = MimeType.getMime(file);
        String charset = MimeType.getCharset(contentType);
        request.response().putHeader("content-type",contentType + (charset != null ? "; charset=" + charset : ""));
        request.response().putHeader("Content-Length", props.size());

        // head support
        if ("HEAD".equals(request.method())) {
            request.response().end();
        } else {
            request.response().sendFile(file);
        }
    }

    private void sendDirectory(final HttpServerRequest request, final String dir, final FileProps props, final Handler<Object> next) {
        final FileSystem fileSystem = vertx.fileSystem();

        fileSystem.readDir(dir, new AsyncResultHandler<String[]>() {
            @Override
            public void handle(AsyncResult<String[]> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(asyncResult.cause());
                } else {

                    String file;
                    StringBuilder files = new StringBuilder("<ul id=\"files\">");

                    for (String s : asyncResult.result()) {
                        file = s.substring(s.lastIndexOf('/') + 1);
                        files.append("<li><a href=\"");
                        files.append(dir);
                        files.append("/");
                        files.append(file);
                        files.append("\" title=\"");
                        files.append(file);
                        files.append("\">");
                        files.append(file);
                        files.append("</a></li>");
                    }

                    files.append("</ul>");

                    // TODO: header
//                    function htmlPath(dir) {
//                            var curr = [];
//                    return dir.split('/').map(function(part){
//                        curr.push(part);
//                        return '<a href="' + curr.join('/') + '">' + part + '</a>';
//                    }).join(' / ');
//                    }

                    request.response().putHeader("Content-Type", "text/html");
                    request.response().end(
                            directoryTemplate.replace("{directory}", dir)
                                    .replace("{linked-path}", "")
                                    .replace("{files}", files.toString()));
                }
            }
        });
    }

    private boolean isFresh(final HttpServerRequest request) {
        // defaults
        boolean etagMatches = true;
        boolean notModified = true;

        // fields
        String modifiedSince = request.headers().get("if-modified-since");
        String noneMatch = request.headers().get("if-none-match");
        String[] noneMatchTokens = null;
        String lastModified = (String) request.response().headers().get("last-modified");
        String etag = (String) request.response().headers().get("etag");

        // unconditional request
        if (modifiedSince == null && noneMatch == null) {
            return false;
        }

        // parse if-none-match
        if (noneMatch != null) {
            noneMatchTokens = noneMatch.split(" *, *");
        }

        // if-none-match
        if (noneMatchTokens != null) {
            etagMatches = false;
            for (String s : noneMatchTokens) {
                if (etag.equals(s) || "*".equals(noneMatchTokens[0])) {
                    etagMatches = true;
                    break;
                }
            }
        }

        // if-modified-since
        if (modifiedSince != null) {
            try {
                Date modifiedSinceDate = ISODATE.parse(modifiedSince);
                Date lastModifiedDate = ISODATE.parse(lastModified);
                notModified = lastModifiedDate.getTime() <= modifiedSinceDate.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                notModified = false;
            }
        }

        return etagMatches && notModified;
    }

    @Override
    public void handle(final HttpServerRequest request, final Handler<Object> next) {
        if (!"GET".equals(request.method()) && !"HEAD".equals(request.method())) {
            next.handle(null);
        } else {
            final String file = root + request.path();
            final FileSystem fileSystem = vertx.fileSystem();

            fileSystem.exists(file, new AsyncResultHandler<Boolean>() {
                @Override
                public void handle(AsyncResult<Boolean> asyncResult) {
                    if (asyncResult.failed()) {
                        next.handle(asyncResult.cause());
                    } else {
                        if (!asyncResult.result()) {
                            // no static file found, let the next middleware handle it
                            next.handle(null);
                        } else {
                            fileSystem.props(file, new AsyncResultHandler<FileProps>() {
                                @Override
                                public void handle(AsyncResult<FileProps> props) {
                                    if (props.failed()) {
                                        next.handle(props.cause());
                                    } else {
                                        // write cache control headers
                                        writeHeaders(request, props.result());
                                        // verify if we are still fresh
                                        if (isFresh(request)) {
                                            request.response().setStatusCode(304);
                                            request.response().end();
                                        } else {
                                            if (props.result().isDirectory()) {
                                                sendDirectory(request, file, props.result(), next);
                                            } else {
                                                sendFile(request, file, props.result());
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
    }
}
