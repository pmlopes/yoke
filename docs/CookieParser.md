#Yoke

## CookieParser

Parse request cookies both signed or plain.

## Usage

Once data has been parsed the result is visible in the field ```cookies``` of the request.

* ```cookies()``` returns ```Map<YokeCookie>```

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new CookieParser()
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If a cooke value starts with *s:* it means that it is a signed cookie. In this case the value is expected to be
*s:<cookie>.<signature>*. The signature is *HMAC + SHA256*.

When the Cookie parser is initialized with a secret then that value is used to verify if a cookie is valid.

* *hmacsha512* hmac+sha512 MAC

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new CookieParser(hmacsha512)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~