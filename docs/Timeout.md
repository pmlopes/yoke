# [Yoke](/)

## Timeout

Times out the request in ```ms```, defaulting to ```5000```.

The timeout error is passed to ```next.handle(408)``` so that you may customize the response behaviour.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new Timeout()
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~