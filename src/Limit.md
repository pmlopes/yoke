# Yoke

## Limit

Limits the request body to a specific amount of bytes. If the request body contains more bytes than the allowed limit an
*413* error is sent back to the client.

### Options

* ```bytes```  max amount of bytes allowed in the request body

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new Limit(1024)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
