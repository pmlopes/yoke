/**
 * Copyright 2011-2014 the original author or authors.
 */
package xyz.jetdrone.yoke;

import com.jetdrone.vertx.yoke.MimeType;
import com.jetdrone.vertx.yoke.util.Utils;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * # Static
 * <p>
 * Static file server with the given ```root``` path. Optionaly will also generate index pages for directory listings.
 */
public class Static implements Handler<Context> {

  /**
   * SimpleDateFormat to format date objects into ISO format.
   */
  private final SimpleDateFormat ISODATE;

  /**
   * Cache for the HTML template of the directory listing page
   */
  private final String directoryTemplate;

  /**
   * Root directory where to look files from
   */
  private final String root;

  /**
   * Max age allowed for cache of resources
   */
  private final long maxAge;

  /**
   * Allow directory listing
   */
  private final boolean directoryListing;

  /**
   * Include hidden files (Hiden files are files start start with dot (.).
   */
  private final boolean includeHidden;

  /**
   * Create a new Static File Server Middleware
   * <p>
   * <pre>
   * new Yoke(...)
   *   .use(new Static("webroot", 0, true, false));
   * </pre>
   *
   * @param root             the root location of the static files in the file system (relative to the main Verticle).
   * @param maxAge           cache-control max-age directive
   * @param directoryListing generate index pages for directories
   * @param includeHidden    in the directory listing show dot files
   */
  public Static(@NotNull String root, final long maxAge, final boolean directoryListing, final boolean includeHidden) {
    // if the root is not empty it should end with / for convenience
    if (!"".equals(root)) {
      if (!root.endsWith("/")) {
        root = root + "/";
      }
    }
    this.root = root;
    this.maxAge = maxAge;
    this.includeHidden = includeHidden;
    this.directoryListing = directoryListing;
    this.directoryTemplate = Utils.readResourceToBuffer(getClass(), "directory.html").toString();

    ISODATE = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
    ISODATE.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /**
   * Create a new Static File Server Middleware that does not generate directory listings or hidden files
   * <p>
   * <pre>
   * new Yoke(...)
   *   .use(new Static("webroot", 0));
   * </pre>
   *
   * @param root   the root location of the static files in the file system (relative to the main Verticle).
   * @param maxAge cache-control max-age directive
   */
  public Static(@NotNull final String root, final long maxAge) {
    this(root, maxAge, false, false);
  }

  /**
   * Create a new Static File Server Middleware that does not generate directory listings or hidden files and files
   * are cache for 1 full day
   * <p>
   * <pre>
   * new Yoke(...)
   *   .use(new Static("webroot"));
   * </pre>
   *
   * @param root the root location of the static files in the file system (relative to the main Verticle).
   */
  public Static(@NotNull final String root) {
    this(root, 86400000, false, false);
  }

  /**
   * Create all required header so content can be cache by Caching servers or Browsers
   *
   * @param ctx
   * @param props
   */
  private void writeHeaders(final Context ctx, final FileProps props) {

    MultiMap headers = ctx.getResponse().headers();

    if (!headers.contains("etag")) {
      headers.set("etag", "\"" + props.size() + "-" + props.lastModifiedTime() + "\"");
    }

    if (!headers.contains("date")) {
      headers.set("date", format(new Date()));
    }

    if (!headers.contains("cache-control")) {
      headers.set("cache-control", "public, max-age=" + maxAge / 1000);
    }

    if (!headers.contains("last-modified")) {
      headers.set("last-modified", format(new Date(props.lastModifiedTime())));
    }
  }

  /**
   * Convert thread safe a date to a string  using a SimpleDateFormat
   *
   * @param date
   */
  private String format(Date date) {
    synchronized (ISODATE) {
      return ISODATE.format(date);
    }
  }

  /**
   * Write a file into the response body
   *
   * @param ctx
   * @param file
   * @param props
   */
  private void sendFile(final Context ctx, final String file, final FileProps props) {
    // write content type
    String contentType = MimeType.getMime(file);
    String charset = MimeType.getCharset(contentType);
    ctx.setType(contentType + "; charset=" + charset);
    ctx.setLength(props.size());

    // head support
    if (ctx.getRequest().method() == HttpMethod.HEAD) {
      ctx.end();
    } else {
      // TODO: handle ranges
      ctx.getResponse().sendFile(file);
    }
  }

  /**
   * Generate Directory listing
   *
   * @param ctx
   * @param dir
   */
  private void sendDirectory(final Context ctx, final String dir) {
    final FileSystem fileSystem = ctx.getApp().getVertx().fileSystem();

    fileSystem.readDir(dir, (AsyncResultHandler<List<String>>) readDir -> {
      if (readDir.failed()) {
        ctx.fail(readDir.cause());
      } else {
        String accept = ctx.get("accept");

        if (accept == null) {
          // default
          accept = "text/plain";
        }

        if (accept.contains("html")) {
          String normalizedDir = dir.substring(root.length());
          if (!normalizedDir.endsWith("/")) {
            normalizedDir += "/";
          }

          String file;
          StringBuilder files = new StringBuilder("<ul id=\"files\">");

          for (String s : readDir.result()) {
            file = s.substring(s.lastIndexOf('/') + 1);
            // skip dot files
            if (!includeHidden && file.charAt(0) == '.') {
              continue;
            }
            files.append("<li><a href=\"");
            files.append(normalizedDir);
            files.append(file);
            files.append("\" title=\"");
            files.append(file);
            files.append("\">");
            files.append(file);
            files.append("</a></li>");
          }

          files.append("</ul>");

          StringBuilder directory = new StringBuilder();
          // define access to root
          directory.append("<a href=\"/\">/</a> ");

          StringBuilder expandingPath = new StringBuilder();
          String[] dirParts = normalizedDir.split("/");
          for (int i = 1; i < dirParts.length; i++) {
            // dynamic expansion
            expandingPath.append("/");
            expandingPath.append(dirParts[i]);
            // anchor building
            if (i > 1) {
              directory.append(" / ");
            }
            directory.append("<a href=\"");
            directory.append(expandingPath.toString());
            directory.append("\">");
            directory.append(dirParts[i]);
            directory.append("</a>");
          }

          ctx.setType("text/html");
          ctx.end(
            directoryTemplate.replace("{title}", (String) ctx.getData().get("title")).replace("{directory}", normalizedDir)
              .replace("{linked-path}", directory.toString())
              .replace("{files}", files.toString()));
        } else if (accept.contains("json")) {
          String file;
          JsonArray json = new JsonArray();

          for (String s : readDir.result()) {
            file = s.substring(s.lastIndexOf('/') + 1);
            // skip dot files
            if (!includeHidden && file.charAt(0) == '.') {
              continue;
            }
            json.add(file);
          }

          ctx.json(json);
        } else {
          String file;
          StringBuilder buffer = new StringBuilder();

          for (String s : readDir.result()) {
            file = s.substring(s.lastIndexOf('/') + 1);
            // skip dot files
            if (!includeHidden && file.charAt(0) == '.') {
              continue;
            }
            buffer.append(file);
            buffer.append('\n');
          }

          ctx.setType("text/plain");
          ctx.end(buffer.toString());
        }
      }
    });
  }

  @Override
  public void handle(@NotNull final Context ctx) {
    final HttpMethod method = ctx.getRequest().method();

    if (method != HttpMethod.GET && method != HttpMethod.HEAD) {
      ctx.next();
    } else {
      String path = ctx.getNormalizedPath();
      // if the normalized path is null it cannot be resolved
      if (path == null) {
        ctx.fail(404);
        return;
      }

      String prefix = ctx.getPrefix();
      int skip = prefix.length();
      if (prefix.endsWith("/")) {
        skip--;
      }

      // map file path from the request
      // the final path is, root + request.path excluding mount
      final String file = root + (skip != 0 ? path.substring(skip) : path);

      if (!includeHidden) {
        int idx = file.lastIndexOf('/');
        String name = file.substring(idx + 1);
        if (name.length() > 0 && name.charAt(0) == '.') {
          ctx.next();
          return;
        }
      }

      final FileSystem fileSystem = ctx.getApp().getVertx().fileSystem();

      fileSystem.exists(file, (AsyncResultHandler<Boolean>) exists -> {
        if (exists.failed()) {
          ctx.fail(exists.cause());
        } else {
          if (!exists.result()) {
            // no static file found, let the next middleware handle it
            ctx.next();
          } else {
            fileSystem.props(file, (AsyncResultHandler<FileProps>) props -> {
              if (props.failed()) {
                ctx.fail(props.cause());
              } else {
                if (props.result().isDirectory()) {
                  if (directoryListing) {
                    // write cache control headers
                    writeHeaders(ctx, props.result());
                    // verify if we are still fresh
                    if (ctx.isFresh()) {
                      ctx.setStatus(304);
                      ctx.end();
                    } else {
                      sendDirectory(ctx, file);
                    }
                  } else {
                    // we are not listing directories
                    ctx.next();
                  }
                } else {
                  // write cache control headers
                  writeHeaders(ctx, props.result());
                  // verify if we are still fresh
                  if (ctx.isFresh()) {
                    ctx.setStatus(304);
                    ctx.end();
                  } else {
                    sendFile(ctx, file, props.result());
                  }
                }
              }
            });
          }
        }
      });
    }
  }
}
