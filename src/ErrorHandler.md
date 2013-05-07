#Yoke

## ErrorHandler

Creates pretty print error pages in *html*, *text* or *json* depending on the *accept* header from the client.

Parameters:

* *boolean* include stack trace. Useful in development mode but probably you don't want it in production.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new ErrorHandler(true)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
