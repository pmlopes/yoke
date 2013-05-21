# [Yoke](/)

## Mozilla Persona

*Persona allows you to sign in to sites using an email address you choose. So, instead of having to manage multiple
usernames and passwords across your favorite sites and devices, you'll have more time to do the important stuff. Mozilla
Will manage the details!*

Adding the Persona login system to your site takes just five steps:

* Include the Persona JavaScript library on your pages.
* Add "login" and "logout" buttons.
* Watch for login and logout actions.
* Verify the user's credentials.
* Review best practices.

This is the description available on the Mozilla website, and indeed it is quite simple to add
Persona authentication to a application built on Yoke and [Vert.x](http://vertx.io)


## Include Persona JS library

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.html}
<!DOCTYPE html>
<html>
<head>
  <title>${title}</title>
  <meta http-equiv="X-UA-Compatible" content="IE=Edge">
  <link rel="stylesheet" href="/css/persona-buttons.css" />
  <script src="//ajax.googleapis.com/ajax/libs/jquery/2.0.0/jquery.min.js"></script>
  <script src="https://login.persona.org/include.js"></script>
</head>
<body>
</body>
</html>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This was very simple. Just save this in the resources directory and later we will use the
[Static](Static.html) middleware to serve it in parallel with the String placeholder engine.


## Add login/logout buttons

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.html .numberLines startFrom="10"}
<body>
  <ul>
      <li><a id="signin" href="#" class="persona-button"><span>Sign in with your Email</span></a></li>
      <li><a id="signout" href="#">Log out</a></li>
  </ul>
  Current User: ${email}
</body>
<script>
  var signinLink = document.getElementById('signin');
  if (signinLink) {
    signinLink.onclick = function() { navigator.id.request(); };
  }

  var signoutLink = document.getElementById('signout');
  if (signoutLink) {
    signoutLink.onclick = function() { navigator.id.logout(); };
  }
</script>
</html>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Again piece of cake!


## Watch for login and logout actions

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.html .numberLines startFrom="28"}
var currentUser = ${email};

  navigator.id.watch({
    loggedInUser: currentUser,
    onlogin: function(assertion) {
      $.ajax({
        type: 'POST',
        url: '/auth/login',
        data: {assertion: assertion},
        success: function(res, status, xhr) { window.location.reload(); },
        error: function(xhr, status, err) {
          navigator.id.logout();
          alert("Login failure: " + err);
        }
      });
    },
    onlogout: function() {
      $.ajax({
        type: 'POST',
        url: '/auth/logout',
        success: function(res, status, xhr) { window.location.reload(); },
        error: function(xhr, status, err) { alert("Logout failure: " + err); }
      });
    }
  });
</script>
</html>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Really?


## Verify the user's credentials

At this moment we enter the server side, so we need to implement a [Static](Static.html)
middleware to serve html, javascript and css files. After that we need to render the right
html, since this example is so simple the string placeholder is perfect. and later we need to
manage the session for the user and the API entry points:

* */* main page
* */auth/login* login end-point
* */auth/logout* logout end-point

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java .numberLines}
public class Persona extends Verticle {

  // This is an example only use a proper persistent storage
  Map<String, String> storage = new HashMap<>();

  @Override
  public void start() {
    final Yoke yoke = new Yoke(vertx);
    yoke.engine("html", new StringPlaceholderEngine());

    Mac secret = Utils.newHmacSHA256("secret here");

    // all environments
    yoke.use(new CookieParser(secret));
    yoke.use(new Session(secret));
    yoke.use(new BodyParser());
    yoke.use(new Static("static"));
    yoke.use(new ErrorHandler(true));

    // routes
    yoke.use(new Router()
      .get("/", new Middleware() {
          @Override
          public void handle(final YokeRequest request, final Handler<Object> next) {
          String sid = request.getSessionId();
          String email = storage.get(sid == null ? "" : sid);

          if (email == null) {
            request.put("email", "null");
          } else {
            request.put("email", "'" + email + "'");
          }
          request.response().render("views/index.html", next);
          }
      })
      .post("/auth/logout", new Middleware() {
          @Override
          public void handle(YokeRequest request, Handler<Object> next) {
          // remove session from storage
          String sid = request.getSessionId();
          storage.remove(sid == null ? "" : sid);
          // destroy session
          request.setSessionId(null);
          // send OK
          request.response().end(new JsonObject().putBoolean("success", true));
          }
      })
      .post("/auth/login", new Middleware() {
          @Override
          public void handle(final YokeRequest request, final Handler<Object> next) {
          String data;

          try {
            // generate the data
            data = "assertion=" + URLEncoder.encode(request.formAttributes().get("assertion"), "UTF-8") +
                "&audience=" + URLEncoder.encode("http://localhost:8080", "UTF-8");
          } catch (UnsupportedEncodingException e) {
            next.handle(e);
            return;
          }

          HttpClient client = getVertx().createHttpClient().setSSL(true).setHost("verifier.login.persona.org").setPort(443);

          HttpClientRequest clientRequest = client.post("/verify", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse response) {
              // error handler
              response.exceptionHandler(new Handler<Throwable>() {
                @Override
                public void handle(Throwable err) {
                  next.handle(err);
                }
              });

              final Buffer body = new Buffer(0);

              // body handler
              response.dataHandler(new Handler<Buffer>() {
                @Override
                public void handle(Buffer buffer) {
                  body.appendBuffer(buffer);
                }
              });
              // done
              response.endHandler(new Handler<Void>() {
                @Override
                public void handle(Void event) {
                  try {
                    JsonObject verifierResp = new JsonObject(body.toString());
                    boolean valid = "okay".equals(verifierResp.getString("status"));
                    String email = valid ? verifierResp.getString("email") : null;
                    if (valid) {
                      // assertion is valid:
                      // generate a session Id
                      String sid = UUID.randomUUID().toString();

                      request.setSessionId(sid);
                      // save it and associate to the email address
                      storage.put(sid, email);
                      // OK response
                      request.response().end(new JsonObject().putBoolean("success", true));
                    } else {
                      request.response().end(new JsonObject().putBoolean("success", false));
                    }
                  } catch (DecodeException ex) {
                    // bogus response from verifier!
                    request.response().end(new JsonObject().putBoolean("success", false));
                  }
                }
              });
            }
          });

          clientRequest.putHeader("content-type", "application/x-www-form-urlencoded");
          clientRequest.putHeader("content-length", Integer.toString(data.length()));
          clientRequest.end(data);
          }
      })
    );

    yoke.listen(8080);
    container.logger().info("Yoke server listening on port 8080");
  }
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

There you go, less than 120 lines and you have a secure site!


## Source code

For the full source code and project files all is available in
[github](https://github.com/pmlopes/yoke/blob/master/example/persona).


## Notes

The application is quite simple and of course the map in memory is not a valid or proper
storage engine. With vert.x is quite simple to integrate data storages such as:

* mongoDB
* redis
* mysql

But this is just a demo of the possibilities of Yoke and Vert.x.