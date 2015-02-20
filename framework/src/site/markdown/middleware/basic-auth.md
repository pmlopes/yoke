## BasicAuth

Enfore basic authentication by providing a AuthHandler.handler(user, pass), which must return true in order to gain
access. Populates request.user. The final alternative is simply passing username / password strings.

### Examples

Creates a new BasicAuth middleware with a master username / password and a given realm.

```
Yoke yoke = new Yoke(...);
yoke.use("/admin", new BasicAuth("admin", "s3cr37",
  "MyApp Auth Required"));
```

Creates a new BasicAuth middleware with a master username / password. By default the realm will be `Authentication
required`.

```
Yoke yoke = new Yoke(...);
yoke.use("/admin", new BasicAuth("admin", "s3cr37"));
```

Creates a new BasicAuth middleware with a AuthHandler and a given realm.

```
Yoke yoke = new Yoke(...);
yoke.use("/admin", new AuthHandler() {
 public void handle(String user, String password, Handler next) {
   // a better example would be fetching user from a DB
   if ("user".equals(user) &amp;&amp; "pass".equals(password)) {
     next.handle(true);
   } else {
     next.handle(false);
   }
 }
}, "My App Auth");
```

Creates a new BasicAuth middleware with a AuthHandler.

```
Yoke yoke = new Yoke(...);
yoke.use("/admin", new AuthHandler() {
 public void handle(String user, String password, Handler next) {
   // a better example would be fetching user from a DB
   if ("user".equals(user) && "pass".equals(password)) {
     next.handle(true);
   } else {
     next.handle(false);
   }
 }
});
```