# [Yoke](/)

## YokeRequest

YokeRequest is an extension to Vert.x *HttpServerRequest* with some helper methods to make it easier to perform common
tasks related to web application development.


## &lt;R&gt; R get(String name)

Allow getting properties in a generified way from the context.

* *name* The key to get
* *&lt;R&gt;* The type of the return

Returns the found object


## &lt;R&gt; R get(String name, R defaultValue)

Allow getting properties in a generified way from the context and return defaultValue if the key does not exist.

* *name* The key to get
* *defaultValue* value returned when the key does not exist
* *&lt;R&gt;* The type of the return

Returns the found object


## &lt;R&gt; R put(String name, R value)

Allows putting a value into the context and return the previous value.

* *name* the key to store
* *value* the value to store
* *&lt;R&gt;* the type of the previous value if present

Returns the previous value or null


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


## String originalMethod()

The original HTTP setMethod for the request. One of GET, PUT, POST, DELETE, TRACE, CONNECT, OPTIONS or HEAD


## long bodyLengthLimit()

Holds the maximum allowed length for the setBody data. -1 for unlimited


## boolean hasBody()

Returns true if this request has a body. In other words: true if content-length or transfer-encoding is present.


## long contentLength()

Returns the content length of this request setBody or -1 if header is not present.


## Object body()

The request setBody and eventually a parsed version of it in json or buffer.


## Buffer bufferBody()

The request setBody casted to buffer or null if it cannot be casted


## Map<String, HttpServerFileUpload> files()

The uploaded files parsed by the body parser middleware.


## Set<YokeCookie> cookies()

The Cookies parsed from the CookieParser middleware.


## void setSessionId(String sessionId)

Update the session cookie with the new session id.


## String getSessionId()

Get the session id from the cookie.


## boolean isSecure()

True if the request is served in a HTTPS server.


## String accepts(String... types)

Check if the given type(s) is acceptable, returning the best match when true, otherwise null, in which case you should
respond with 406 "Not Acceptable".

he type value must be a single mime type string such as "application/json" and is validated by checking if the request
string starts with it.


## String ip()

Returns the ip address of the client, when trust-proxy is true (default) then first look into X-Forward-For header.


## HttpServerRequest vertxHttpServerRequest()

Return the real request.
