# Yoke

## MethodOverride

Pass an optional ```key``` to use when checking for a method override, othewise defaults to *_method* or the header
*x-http-method-override*. The original method is available via ```req.originalMethod```.

* *String* key

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new MethodOverride()
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
