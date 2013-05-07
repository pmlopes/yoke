# Yoke

## Router

Route request by path or regular expression. All *HTTP* verbs are available:

* ```GET```
* ```PUT```
* ```POST```
* ```DELETE```
* ```OPTIONS```
* ```HEAD```
* ```TRACE```
* ```CONNECT```
* ```PATCH```

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new Router() {{
  get("/hello", new Handler<YokeHttpServerRequest>() {
    public void handle(YokeHttpServerRequest request) {
      request.response().end("Hello World!");
    }
  });
}}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~