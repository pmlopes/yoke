package xyz.jetdrone.yoke;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;

public interface Context {

  // TODO: cookies, uploads, render, body

  Logger LOG = LoggerFactory.getLogger(Context.class);

  Map<String, Object> getData();

  void next();

  void fail(int status, String message);

  void fail(int status, String message, Throwable cause);

  HttpServerRequest getRequest();

  HttpServerResponse getResponse();

  // begin default implementation

  void fail(int statusCode);

  void fail(String errorMessage);

  void fail(Throwable throwable);

  void fail(int statusCode, Throwable throwable);

  /**
   * Return the prefix where the current handler was used
   *
   * @return the route prefix
     */
  String getPrefix();

  /**
   * Set Content-Disposition get to "attachment" with optional `filename`.
   *
   * @param filename a file name
   */
  void attachment(String filename);

  /**
   * Perform a 302 redirect to `url`.
   * <p/>
   * The string "back" is special-cased
   * to provide Referrer support
   * <p/>
   * Examples:
   * <p/>
   * this.redirect('back');
   * this.redirect('/login');
   * this.redirect('http://google.com');
   *
   * @param url the URL
   */
  void redirect(String url);

  /**
   * Perform a 302 redirect to `url`.
   * <p/>
   * The string "back" is special-cased
   * to provide Referrer support, when Referrer
   * is not present `alt` or "/" is used.
   * <p/>
   * Examples:
   * <p/>
   * this.redirect('back');
   * this.redirect('back', '/index.html');
   * this.redirect('/login');
   * this.redirect('http://google.com');
   *
   * @param url the URL
   * @param alt alternative
   */
  void redirect(String url, String alt);

  /**
   * Remove header `field`.
   *
   * @param field header to remove
   */
  void remove(String field);

  /**
   * Vary on `field`.
   *
   * @param field what
   */
  void vary(String field);

  /**
   * Set header `field` to `val`.
   * <p/>
   * Example:
   * <p/>
   * this.putAt('Accept', 'application/json');
   *
   * @param field name
   * @param val value
   */
  void set(String field, String val);

  /**
   * Append additional header `field` with value `val`.
   * <p/>
   * Example:
   * <p/>
   * this.append('Set-Cookie', 'foo=bar; Path=/; HttpOnly');
   * this.append('Warning', '199 Miscellaneous warning');
   *
   * @param field name
   * @param val value
   */
  void append(String field, String val);

  /**
   * Set getResponse status code.
   *
   * @param code status
   */
  void setStatus(int code);

  /**
   * Set getResponse status message
   *
   * @param msg message
   */
  void setMessage(String msg);

  void end(String chunk);

  void binary(Buffer chunk);

  void end();

  void json(Object bean);

  /**
   * Set Content-Length field to `n`.
   *
   * @param n nr of bytes
   */
  void setLength(long n);

  /**
   * Content-Type response with `type` through `mime.lookup()`
   * when it does not contain a charset.
   * <p/>
   * Examples:
   * <p/>
   * this.type = '.html';
   * this.type = 'html';
   * this.type = 'json';
   * this.type = 'application/json';
   * this.type = 'png';
   *
   * @param type content type
   */
  void setType(String type);

  /**
   * Set the Last-Modified date using a string or a Date.
   * <p/>
   * this.getResponse.lastModified = new Date();
   * this.getResponse.lastModified = '2013-09-13';
   *
   * @param val date
   */
  void setLastModified(Instant val);

  void setLastModified(String val);

  /**
   * Set the ETag of a getResponse.
   * This will normalize the quotes if necessary.
   * <p/>
   * this.getResponse.etag = 'md5hashsum';
   * this.getResponse.etag = '"md5hashsum"';
   * this.getResponse.etag = 'W/"123456789"';
   *
   * @param val etag
   */
  void setEtag(String val);

  /**
   * Check if a get has been written to the socket.
   *
   * @return boolean
   */
  boolean isHeaderSent();

//  default void addCookie(HttpCookie cookie) {
//    getResponse().addCookie(cookie);
//  }
//
//  default void removeCookie(String name) {
//    getResponse().removeCookie(name);
//  }

//  default String acceptsLanguages(@NotNull String... lang) {
//    return getRequest().acceptsLanguages(lang);
//  }
//
//  default String acceptsEncodings(@NotNull String... encoding) {
//    return getRequest().acceptsEncodings(encoding);
//  }
//
//  default String acceptsCharsets(@NotNull String... charset) {
//    return getRequest().acceptsCharsets(charset);
//  }

  /**
   * Check if the given `type(s)` is acceptable, returning
   * the best match when true, otherwise `undefined`, in which
   * case you should respond with 406 "Not Acceptable".
   * <p/>
   * The `type` value may be a single mime type string
   * such as "application/json", the extension name
   * such as "json" or an array `["json", "html", "text/plain"]`. When a list
   * or array is given the _best_ match, if any is returned.
   * <p/>
   * Examples:
   * <p/>
   * // Accept: text/html
   * this.accepts('html');
   * // => "html"
   * <p/>
   * // Accept: text/*, application/json
   * this.accepts('html');
   * // => "html"
   * this.accepts('text/html');
   * // => "text/html"
   * this.accepts('json', 'text');
   * // => "json"
   * this.accepts('application/json');
   * // => "application/json"
   * <p/>
   * // Accept: text/*, application/json
   * this.accepts('image/png');
   * this.accepts('png');
   * // => null
   * <p/>
   * // Accept: text/*;q=.5, application/json
   * this.accepts(['html', 'json']);
   * this.accepts('html', 'json');
   * // => "json"
   *
   * @param type list of accepted content types
   * @return the best matching accepted type
   */
  String accepts(@NotNull String... type);

  /**
   * Return request header.
   * <p>
   * The `Referrer` get field is special-cased,
   * both `Referrer` and `Referer` are interchangeable.
   * <p>
   * Examples:
   * <p>
   * this.get('Content-Type');
   * // => "text/plain"
   * <p>
   * this.get('content-type');
   * // => "text/plain"
   * <p>
   * this.get('Something');
   * // => null
   *
   * @param name get name
   * @return the value for the get
   */
  String get(@NotNull String name);

  /**
   * Check if the incoming getRequest contains the "Content-Type"
   * get field, and it contains the give mime `type`.
   * If there is no getRequest body, `false` is returned.
   * If there is no content type, `false` is returned.
   * Otherwise, it returns true if the `type` that matches.
   * <p/>
   * Examples:
   * <p/>
   * // With Content-Type: text/html; getCharset=utf-8
   * this.is('html'); // => true
   * this.is('text/html'); // => true
   * <p/>
   * // When Content-Type is application/json
   * this.is('application/json'); // => true
   * this.is('html'); // => false
   *
   * @param type content type
   * @return The most close value
   */
  boolean is(@NotNull String type);

//  default void setMethod(@NotNull Method method) {
//    getRequest().setMethod(method);
//  }
//
//  default void setPath(String path) {
//    getRequest().setPath(path);
//  }
//
//  default void setQuery(String obj) {
//    getRequest().setQuery(obj);
//  }
//
//  default void setURI(String val) {
//    getRequest().setURI(val);
//  }

  /**
   * Check if the getRequest is idempotent.
   *
   * @return idempotent
   */
  boolean isIdempotent();

  /**
   * Return subdomains as an array.
   * <p/>
   * Subdomains are the dot-separated parts of the host before the main domain of
   * the app. By default, the domain of the app is assumed to be the last two
   * parts of the host.
   * <p/>
   * For example, if the domain is "tobi.ferrets.example.com":
   * If `app.subdomainOffset` is not set, this.subdomains is `["ferrets", "tobi"]`.
   *
   * @return list
   */
  List<String> getSubdomains();

  /**
   * Return the protocol string "http" or "https"
   * when requested with TLS. When the proxy setting
   * is enabled the "X-Forwarded-Proto" get
   * field will be trusted. If you're running behind
   * a reverse proxy that supplies https for you this
   * may be enabled.
   *
   * @return {String}
   */
  String getProtocol();

  /**
   * The path part of the URI. For example /somepath/somemorepath/someresource.foo
   */
  String getPath();

  String getNormalizedPath();

  /**
   * Parse the "Host" get field host
   * and support X-Forwarded-Host when a
   * proxy is enabled.
   *
   * @return hostname:port
   */
  String getHost();

  /**
   * Parse the "Host" get field hostname
   * and support X-Forwarded-Host when a
   * proxy is enabled.
   *
   * @return hostname
   */
  String getHostname();

  /**
   * Returns all header names for this Request.
   */
  MultiMap getHeaders();

  /**
   * Short-hand for:
   * <p/>
   * this.protocol == 'https'
   *
   * @return boolean
   */
  boolean isSecure();
  /**
   * Check if the getRequest is stale, aka
   * "Last-Modified" and / or the "ETag" for the
   * resource has changed.
   *
   * @return boolean
   */
  boolean isStale();

  /**
   * Check if the getRequest is fresh, aka
   * Last-Modified and/or the ETag
   * still match.
   *
   * @return boolean
   */
  boolean isFresh();

  /**
   * When `app.proxy` is `true`, parse
   * the "X-Forwarded-For" ip address list.
   * <p/>
   * For example if the value were "client, proxy1, proxy2"
   * you would receive the array `["client", "proxy1", "proxy2"]`
   * where "proxy2" is the furthest down-stream.
   *
   * @return {Array}
   */
  List<String> getIps();

  /**
   * Return the remote address, or when
   * `app.proxy` is `true` return
   * the upstream addr.
   *
   * @return {String}
   */
  String getIp();

//  default Iterable<HttpCookie> getCookies() {
//    return getRequest().getCookies();
//  }
//
//  default HttpCookie getCookie(@NotNull String name) {
//    return getRequest().getCookie(name);
//  }
}
