package xyz.jetdrone.yoke.impl;

import com.jetdrone.vertx.yoke.MimeType;
import com.jetdrone.vertx.yoke.util.Utils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.Json;
import org.jetbrains.annotations.NotNull;
import xyz.jetdrone.yoke.Context;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpResponseStatus.TEMPORARY_REDIRECT;
import static io.netty.handler.codec.http.HttpResponseStatus.USE_PROXY;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public final class ContextImpl extends AbstractContext implements Context {

  private static final List<HttpMethod> IDEMPOTENT_METHODS = Collections.unmodifiableList(Arrays.asList(
    HttpMethod.GET,
    HttpMethod.HEAD,
    HttpMethod.PUT,
    HttpMethod.DELETE,
    HttpMethod.OPTIONS,
    HttpMethod.TRACE
  ));

  private static final int[] REDIRECT_STATUSES = new int[]{
    MULTIPLE_CHOICES.code(),
    MOVED_PERMANENTLY.code(),
    FOUND.code(),
    SEE_OTHER.code(),
    USE_PROXY.code(),
    TEMPORARY_REDIRECT.code(),
    308 /* PERMANENT_REDIRECT */
  };

  private static final Pattern NOQUOTES = Pattern.compile("^(W/)?\"");

  private static String acceptsHeader(@NotNull String header, @NotNull String... type) {
    // parse
    String[] acceptTypes = header.split(" *, *");
    // sort on quality
    Arrays.sort(acceptTypes, ACCEPT_X_COMPARATOR);

    for (String senderAccept : acceptTypes) {
      String[] sAccept = splitMime(senderAccept);

      for (String appAccept : type) {
        String[] aAccept = splitMime(appAccept);

        if ((sAccept[0].equals(aAccept[0]) || "*".equals(sAccept[0]) || "*".equals(aAccept[0])) &&
          (sAccept[1].equals(aAccept[1]) || "*".equals(sAccept[1]) || "*".equals(aAccept[1]))) {
          return senderAccept;
        }
      }
    }

    return null;
  }

  private static String[] splitMime(@NotNull String mime) {
    // find any ; e.g.: "application/json;q=0.8"
    int space = mime.indexOf(';');

    if (space != -1) {
      mime = mime.substring(0, space);
    }

    String[] parts = mime.split("/");

    if (parts.length < 2) {
      return new String[]{
        parts[0],
        "*"
      };
    }

    return parts;
  }

  private static final Comparator<String> ACCEPT_X_COMPARATOR = (o1, o2) -> {
    float f1 = getQuality(o1);
    float f2 = getQuality(o2);
    if (f1 < f2) {
      return 1;
    }
    if (f1 > f2) {
      return -1;
    }
    return 0;
  };

  private static String getParameter(@NotNull final String headerValue, @NotNull final String parameter) {
    String[] params = headerValue.split(" *; *");
    for (int i = 1; i < params.length; i++) {
      String[] parameters = params[1].split(" *= *");
      if (parameter.equals(parameters[0])) {
        return parameters[1];
      }
    }
    return null;
  }

  private static float getQuality(final String headerValue) {
    if (headerValue == null) {
      return 0;
    }

    final String q = getParameter(headerValue, "q");

    if (q == null) {
      return 1;
    }

    return Float.parseFloat(q);
  }


  // start of implementation

  @Override
  public void fail(int statusCode) {
    fail(statusCode, HttpResponseStatus.valueOf(statusCode).reasonPhrase());
  }

  @Override
  public void fail(String errorMessage) {
    fail(500, errorMessage);
  }

  @Override
  public void fail(Throwable throwable) {
    fail(505, throwable);
  }

  @Override
  public void fail(int statusCode, Throwable throwable) {
    if (throwable != null) {
      LOG.debug(throwable.getMessage(), throwable);
      fail(statusCode, throwable.getMessage(), throwable);
    } else {
      fail(statusCode);
    }
  }

  @Override
  public void attachment(String filename) {
    if (filename != null) {
      setType(MimeType.getMime(filename));
    }
    set("Content-Disposition", "attachment; filename=" + filename);
  }

  @Override
  public void redirect(String url) {
    redirect(url, null);
  }

  @Override
  public void redirect(String url, String alt) {
    // location
    if ("back".equals(url)) {
      url = get("Referrer");
      if (url == null) {
        url = alt;
      }
      if (url == null) {
        url = "/";
      }
    }

    set("Location", url);

    // status
    int status = getResponse().getStatusCode();
    if (Arrays.binarySearch(REDIRECT_STATUSES, status) == -1) {
      setStatus(302 /* FOUND */);
    }

    // html
    if (accepts("html") != null) {
      try {
        url = URLEncoder.encode(url, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
      setType("text/html; charset=utf-8");
      end("Redirecting to <a href=\"" + url + "\">" + url + "</a>.");
      return;
    }

    // text
    setType("text/plain; charset=utf-8");
    end("Redirecting to " + url + ".");
  }

  @Override
  public void remove(String field) {
    getResponse().headers().remove(field);
  }

  @Override
  public void vary(String field) {
    append("Vary", field);
  }

  @Override
  public void set(String field, String val) {
    getResponse().headers().set(field, val);
  }

  @Override
  public void append(String field, String val) {
    getResponse().headers().add(field, val);
  }

  @Override
  public void setStatus(int code) {
    getResponse().setStatusCode(code);
  }

  @Override
  public void setMessage(String msg) {
    getResponse().setStatusMessage(msg);
  }

  @Override
  public void end(String chunk) {
    if (chunk == null) {
      end();
    } else {
      getResponse().end(chunk);
    }
  }

  @Override
  public void binary(byte[] chunk) {
    if (chunk == null) {
      end();
    } else {
      getResponse().end(Buffer.buffer(chunk));
    }
  }

  @Override
  public void end() {
    getResponse().end();
  }

  @Override
  public void json(Object bean) {
    if (bean == null) {
      end();
    } else {
      try {
        final String encoded = Json.encode(bean);
        setType("application/json; charset=utf-8");
        end(encoded);
      } catch (EncodeException e) {
        fail(e);
      }
    }
  }

  @Override
  public void setLength(long n) {
    set("Content-Length", Long.toString(n));
  }

  @Override
  public void setType(String type) {
    set("Content-Type", MimeType.getMime(type));
  }

  @Override
  public void setLastModified(Instant val) {
    setLastModified(OffsetDateTime.ofInstant(val, ZoneOffset.UTC).format(ISO_OFFSET_DATE_TIME));
  }

  @Override
  public void setLastModified(String val) {
    set("Last-Modified", val);
  }

  @Override
  public void setEtag(String val) {
    if (!NOQUOTES.matcher(val).matches()) {
      set("ETag", "\"" + val + "\"");
    } else {
      set("ETag", val);
    }
  }

  @Override
  public boolean isHeaderSent() {
    return getResponse().headWritten();
  }

//  @Override public void addCookie(HttpCookie cookie) {
//    getResponse().addCookie(cookie);
//  }
//
//  @Override public void removeCookie(String name) {
//    getResponse().removeCookie(name);
//  }

//  @Override public String acceptsLanguages(@NotNull String... lang) {
//    return getRequest().acceptsLanguages(lang);
//  }
//
//  @Override public String acceptsEncodings(@NotNull String... encoding) {
//    return getRequest().acceptsEncodings(encoding);
//  }
//
//  @Override public String acceptsCharsets(@NotNull String... charset) {
//    return getRequest().acceptsCharsets(charset);
//  }

  @Override
  public String accepts(@NotNull String... type) {
    String accept = get("Accept");
    // accept anything when accept is not present
    if (accept == null) {
      return type[0];
    }

    return acceptsHeader(accept, type);
  }

  @Override
  public String get(@NotNull String name) {
    if ("referer".equalsIgnoreCase(name) || "referrer".equalsIgnoreCase(name)) {
      final String header = getRequest().getHeader("referrer");
      return (header != null) ? header : getRequest().getHeader("referer");
    }

    return getRequest().getHeader(name);
  }

  @Override
  public boolean is(@NotNull String type) {
    String ct = get("Content-Type");
    if (ct == null) {
      return false;
    }
    // get the content type only (exclude getCharset)
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

//  @Override public void setMethod(@NotNull Method method) {
//    getRequest().setMethod(method);
//  }
//
//  @Override public void setPath(String path) {
//    getRequest().setPath(path);
//  }
//
//  @Override public void setQuery(String obj) {
//    getRequest().setQuery(obj);
//  }
//
//  @Override public void setURI(String val) {
//    getRequest().setURI(val);
//  }

  @Override
  public boolean isIdempotent() {
    return IDEMPOTENT_METHODS.contains(getRequest().method());
  }

  @Override
  public List<String> getSubdomains() {
    final String host = getHost();
    if (host == null) {
      return Collections.emptyList();
    }

    final String[] subdomains = host.split("\\.");
    Integer subdomainOffset = (Integer) getData().get("subdomainOffset");

    if (subdomainOffset == null) {
      subdomainOffset = 2;
    }

    if (subdomains.length > subdomainOffset) {
      Arrays.sort(subdomains, Collections.reverseOrder());
      final List<String> list = new ArrayList<>(subdomains.length - subdomainOffset);
      for (int i = 0; i < list.size(); i++) {
        list.add(i, subdomains[i]);
      }

      return list;
    }

    return Collections.emptyList();
  }

  @Override
  public String getProtocol() {
    final Boolean proxy = (Boolean) getData().get("proxy");

    if (getRequest().isSSL() || proxy == null || !proxy) {
      return getRequest().scheme();
    }

    final String proto = get("X-Forwarded-Proto");

    if (proto == null) {
      return getRequest().scheme();
    }

    return proto.split("\\s*,\\s*")[0];
  }

  @Override
  public String getPath() {
    return getRequest().path();
  }

  private String cachedNormalizedPath = null;

  @Override
  public String getNormalizedPath() {
    if (cachedNormalizedPath != null) {
      return cachedNormalizedPath;
    }

    // plus sign have a special meaning in the path and should not be decoded
    String path = Utils.decodeURIComponent(getRequest().path().replaceAll("\\+", "%2B"));

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
  public String getHost() {
    final Boolean proxy = (Boolean) getData().get("proxy");
    String host = null;

    if (proxy != null && proxy) {
      host = get("X-Forwarded-Host");
    }

    if (host == null) {
      host = get("Host");
    }

    if (host != null) {
      return host.split("\\s*,\\s*")[0];
    }

    return getRequest().host();
  }

  @Override
  public String getHostname() {
    final String host = getHost();
    if (host != null) {
      return host.split(":")[0];
    }

    return null;
  }

  @Override
  public MultiMap getHeaders() {
    return getRequest().headers();
  }

  @Override
  public boolean isSecure() {
    return getRequest().isSSL();
  }

  @Override
  public boolean isStale() {
    return !isFresh();
  }

  @Override
  public boolean isFresh() {
    final HttpMethod method = getRequest().method();

    // GET or HEAD for weak freshness validation only
    if (method != HttpMethod.GET && method != HttpMethod.HEAD) {
      return false;
    }

    final int s = getResponse().getStatusCode();
    // 2xx or 304 as per rfc2616 14.26
    if ((s >= 200 && s < 300) || 304 == s) {
      // @Override publics
      boolean etagMatches = true;
      boolean notModified = true;

      // fields
      String modifiedSince = get("If-Modified-Since");
      String noneMatch = get("If-None-Match");
      String lastModified = get("Last-Modified");
      String etag = get("ETag");
      String cc = get("Cache-Control");

      // unconditional request
      if (modifiedSince == null && noneMatch == null) {
        return false;
      }

      // check for no-cache cache request directive
      if (cc != null && cc.contains("no-cache")) {
        return false;
      }

      // parse if-none-match
      String[] noneMatches = null;

      if (noneMatch != null) {
        noneMatches = noneMatch.split(" *, *");
      }

      // if-none-match
      if (noneMatches != null) {
        for (final String match : noneMatches) {
          if ("*".equals(match) || match.equals(etag) || match.equals("W/" + etag)) {
            etagMatches = true;
            break;
          }
          etagMatches = false;
        }
      }

      // if-modified-since
      if (modifiedSince != null) {
        long modifiedSinceMillis = Date.parse(modifiedSince);
        long lastModifiedMillis = Date.parse(lastModified);
        notModified = lastModifiedMillis <= modifiedSinceMillis;
      }

      return etagMatches && notModified;
    }

    return false;
  }

  @Override
  public List<String> getIps() {
    final Boolean proxy = (Boolean) getData().get("proxy");
    final String val = get("X-Forwarded-For");

    if (proxy != null && proxy && val != null) {
      return Arrays.asList(val.split(" *, *"));
    }

    return Collections.emptyList();
  }

  @Override
  public String getIp() {
    final Boolean proxy = (Boolean) getData().get("proxy");

    if (proxy != null && proxy) {
      List<String> ips = getIps();
      if (ips.size() > 0) {
        return ips.get(0);
      }
    }

    return getRequest().remoteAddress().toString();
  }

//  @Override public Iterable<HttpCookie> getCookies() {
//    return getRequest().getCookies();
//  }
//
//  @Override public HttpCookie getCookie(@NotNull String name) {
//    return getRequest().getCookie(name);
//  }


  private final Map<String, Object> globals;

  private String prefix;

  private ShadowMap data;

  public ContextImpl(@NotNull HttpServerRequest req, @NotNull Map<String, Object> globals) {
    super(req);
    this.globals = globals;
  }

  @Override
  public Map<String, Object> getData() {
    if (data == null) {
      data = new ShadowMap(globals);
    }

    return data;
  }

  @Override
  public String getPrefix() {
    return prefix;
  }

  protected void setPrefix(String prefix) {
    this.prefix = prefix;
  }
}
