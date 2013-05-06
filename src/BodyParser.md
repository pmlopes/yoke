# Yoke

## BodyParser

Parse request bodies, supports *application/json*, *application/x-www-form-urlencoded*, and *multipart/form-data*.

Once data has been parsed the result is visible in the field <code>body</code> of the request. To help there are 3
helper getters for this field:

* ```bodyJson()``` returns ```JsonObject```
* ```bodyMap()``` returns ```Map```
* ```bodyBuffer()``` returns ```Buffer```

If the content type was *multipart/form-data* and there were uploaded files the files are:

* ```files()``` returns ```Map<String, UploadFile>```

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
new BodyParser()
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
