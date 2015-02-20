## Logger

Logger for request. There are 3 formats included:
* DEFAULT
* SHORT
* TINY

Default tries to log in a format similar to Apache log format, while the other 2 are more suited to development mode.
The logging depends on Vert.x logger settings and the severity of the error, so for errors with status greater or
equal to 500 the fatal severity is used, for status greater or equal to 400 the error severity is used, for status
greater or equal to 300 warn is used and for status above 100 info is used.