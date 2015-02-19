# Compress

Middleware to compress responses and set the appropriate response headers.
Not all responses are compressed, the middleware first inspects if the
request accepts compression and tries to select the best matched algorithm.

You can specify which content types are compressable and by default json/text/javascript
are enabled.