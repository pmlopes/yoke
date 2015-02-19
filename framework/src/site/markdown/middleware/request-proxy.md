# RequestProxy

RequestProxy provides web client a simple way to interact with other REST service
providers via Yoke, meanwhile Yoke could pre-handle authentication, logging and etc.

In order to handler the proxy request properly, Bodyparser should be disabled for the
path matched by RequestProxy.