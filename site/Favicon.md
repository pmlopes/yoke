# [Yoke](/)

## Favicon

By default serves the Yoke favicon, or the favicon located by the given ```path```.

## Usage

* *String* ```path``` the location of your favicon file
* *long* ```maxAge``` cache-control max-age directive, defaulting to 1 day

Using Yoke favicon and default max-age:
``` java
new Favicon()
```

Using your favicon and default max-age:

``` java
new Favicon("favicon.ico")
```

Using your favicon and custom max-age:

``` java
new Favicon("favicon.ico", 1000)
```