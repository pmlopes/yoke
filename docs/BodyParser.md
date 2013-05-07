# [Yoke](/)

## BodyParser

Parse request bodies, supports *application/json*, *application/x-www-form-urlencoded*, and *multipart/form-data*.

Once data has been parsed the result is visible in the field ```body``` of the request. To help there are 3
helper getters for this field:

* ```bodyJson()``` returns ```JsonObject```
* ```bodyMap()``` returns ```Map```
* ```bodyBuffer()``` returns ```Buffer```

If the content type was *multipart/form-data* and there were uploaded files the files are:

* ```files()``` returns ```Map<String, UploadFile>```

## Usage

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new BodyParser()
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

## Defines

Defines the variable ```body``` in the request. This variable is of type ```Object``` there are 3 helpers to cast the
value to:

* *JsonObject* ```bodyJson()```
* *Map* ```bodyMap()```
* *Buffer* ```bodyBuffer()```


## Limitations

Currently when parsing *multipart/form-data* if there are several files uploaded under the same name, only the last is
preserved.