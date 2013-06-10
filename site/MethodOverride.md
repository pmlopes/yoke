# [Yoke](/)

## MethodOverride

Pass an optional ```key``` to use when checking for a method override, othewise defaults to *_method* or the header
*x-http-method-override*. The original method is available via ```req.originalMethod```.


## Usage

``` java
new MethodOverride()
```

Or with optional key name

* *String* key

``` java
new MethodOverride("x_method")
```


## Notes

If the method override is sent in a *POST* then the [BodyParser](BodyParser.html) middleware must be used and installed prior this one.