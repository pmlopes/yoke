# [Yoke](/)

## Static

Static file server with the given ```root``` path. Optionaly will also generate index pages for directory listings.

### Options

* ```root``` the root location of the static files in the file system
* ```maxAge``` cache-control max-age directive
* ```directoryListing``` generate index pages for directories
* ```includeHidden``` in the directory listing show dot files

``` java
new Static("webroot", 0, true, false)
```