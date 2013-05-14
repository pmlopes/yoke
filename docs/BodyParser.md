# [Yoke](/)

## BodyParser

Parse request bodies, supports *application/json*, *application/x-www-form-urlencoded*, and *multipart/form-data*.

Once data has been parsed the result is visible in the field ```body``` of the request. To help there are 2
helper getters for this field:

* ```bodyJson()``` returns ```JsonObject```
* ```bodyBuffer()``` returns ```Buffer```

If the content type was *multipart/form-data* and there were uploaded files the files are:

* ```files()``` returns ```Map<String, HttpServerFileUpload>```

## Usage

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new BodyParser()
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

## Defines

Defines the variable ```body``` in the request. This variable is of type ```Object``` there are 2 helpers to cast the
value to:

* *JsonObject* ```bodyJson()```
* *Buffer* ```bodyBuffer()```


## Limitations

Currently when parsing *multipart/form-data* if there are several files uploaded under the same name, only the last is
preserved. Same with attributes when *application/x-www-form-urlencoded*.