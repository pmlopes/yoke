# [Yoke](/)

## YokeResponse

YokeResponse is an extension to Vert.x *HttpServerResponse* with some helper methods to make it easier to perform common
tasks related to web application development.

## render(final String template, final Handler<Object> next)

Renders a template using the registered template engines and use the handler to report errors or success.


## render(final String template)

Renders a template using the registered template engines.


## &lt;R&gt; R getHeader(String name)

Allow getting headers in a generified way.

* *name* The key to get
* *&lt;R&gt;* The type of the return

Returns the found header value


## &lt;R&gt; R getHeader(String name, R defaultValue)

Allow getting headers in a generified way from the context and return defaultValue if the key does not exist.

* *name* The key to get
* *defaultValue* value returned when the key does not exist
* *&lt;R&gt;* The type of the return

Returns the found object.


## void redirect(String url)

Send a HTTP status code 302 with a redirect to the specific url.


## void void redirect(int status, String url)

Send a HTTP status code with a redirect to the specific url.


## end(JsonElement json)

Allows sending Json as a response with the correct content type header.


## jsonp(String callback = "callback", JsonElement json)

Allows sending Json as a response with the correct content type header as a JSONP response.


## jsonp(String callback = "callback", String body)

Allows sending serialized Json as a response with the correct content type header as a JSONP response.


## void end(ReadStream<?> stream)

Pumps a Stream directly to the response object.


## void addCookie(Cookie cookie)

Adds a cookie to the response.


## void headersHandler(Handler<Void> handler)

Allows to receive notifications when headers are about to be written.


## void endHandler(Handler<Void> handler)

Allows to receive notifications when the response ended.
