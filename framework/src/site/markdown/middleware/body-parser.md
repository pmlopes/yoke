## BodyParser

Parse request bodies, supports *application/json*, *application/x-www-form-urlencoded*, and *multipart/form-data*.

Once data has been parsed the result is visible in the field `body` of the request.

If the content type was *multipart/form-data* and there were uploaded files the files are ```files()``` returns
`Map&lt;String, HttpServerFileUpload&gt;`.

### Limitations

Currently when parsing *multipart/form-data* if there are several files uploaded under the same name, only the last
is preserved.

### Examples

Instantiates a Body parser with a configurable upload directory.

```
Yoke yoke = new Yoke(...);
yoke.use(new BodyParser("/upload"));
```

Instantiates a Body parser using the system default temp directory.

```
Yoke yoke = new Yoke(...);
yoke.use(new BodyParser());
```