# Yoke

## BasicAuth

Enfore basic authentication by providing a ```BasicAuth.AuthHandler``` interface, which must return ```true``` in order
to gain access. Alternatively an fixed username, password is provided. The middleware populates ```request.user```.

* *String* username
* *String* password

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new BasicAuth("username", "password")
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* *String* username
* *String* password
* *String* realm

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new BasicAuth("username", "password", "Authentication Required")
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* *BasicAuth.AuthHandler* async handler to allow fetching user data asynchronous

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new BasicAuth(new BasicAuth.AuthHandler() {
  @Override
  public void handle(String username, String password, Handler<Boolean> result) {
    if ("username".equals(username) && "password".equals("password")) {
      result.handle(true);
    } else {
      result.handle(false);
    }
  }
});
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* *BasicAuth.AuthHandler* async handler to allow fetching user data asynchronous
* *String* realm

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new BasicAuth(new BasicAuth.AuthHandler() {
  @Override
  public void handle(String username, String password, Handler<Boolean> result) {
    if ("username".equals(username) && "password".equals("password")) {
      result.handle(true);
    } else {
      result.handle(false);
    }
  }
}, "Authentication required");
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~