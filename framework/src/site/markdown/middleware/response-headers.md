# Response Headers

A simple Middleware that allows adding custom response headers to all YokeResponse.


## Examples

```
yoke.use(new ResponseHeaders()
  .with("X-Build-Meta", "1.0-SNAPSHOT")
  .with("X-Server-Id", "server-123"));
}
```