# [Yoke](/)

## BasicAuth

Enfore basic authentication by providing a ```BasicAuth.AuthHandler``` interface, which must return ```true``` in order
to gain access. Alternatively an fixed username, password is provided. The middleware populates ```request.user```.


## Usage

### Username and password

* *String* username
* *String* password

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new BasicAuth("username", "password")
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

### Username, password and realm name

* *String* username
* *String* password
* *String* realm

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new BasicAuth("username", "password", "Authentication Required")
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

### Authentication handler

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

### Authentication handler and realm name

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


## Defines

Defines the property ```user``` in the request context.


## Extensions

When there is a need to support several realms under the same server, you can override the the ```getRealm``` method.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
public String getRealm(YokeHttpServerRequest request) {
  // do something with the request to generate the valid realm name
  return "Authentication is required to access this resource";
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
