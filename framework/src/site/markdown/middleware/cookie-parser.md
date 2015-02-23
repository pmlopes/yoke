## CookieParser

 Parse request cookies both signed or plain.

If a cooke value starts with *s:* it means that it is a signed cookie. In this case the value is expected to be
*s:&lt;cookie&gt;.&lt;signature&gt;*. The signature is *HMAC + SHA256*.

When the Cookie parser is initialized with a secret then that value is used to verify if a cookie is valid.

### Examples

Instantiates a CookieParser with a given Mac.

```
Yoke yoke = new Yoke(...);
yoke.use(new CookieParser(YokeSecurity.newHmacSHA256("s3cr3t")));
```

Instantiates a CookieParser without a Mac. In this case no cookies will be signed.

```
Yoke yoke = new Yoke(...);
yoke.use(new CookieParser());
```