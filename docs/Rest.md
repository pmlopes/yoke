# [Yoke](/)

## JsonRestRouter

JsonRestRouter is a extra middleware module built specifically to enable you to build correct REST web services. It
intentionally extends Yoke [Router](Router.html) middleware as that is more or less the de facto API for writing web
applications on top of Yoke.

In short, JsonRestRouter adds a data store backend to a router and implements all basic content negotiation (just JSON)
and all the REST semantics.

There are 6 verbs:

* *QUERY* - maps to HTTP GET /resource
* *READ* - maps to HTTP GET /resource/:id
* *UPDATE* - maps to HTTP PUT /resource/:id
* *APPEND* - maps to HTTP PATCH /resource/:id
* *CREATE* - maps to HTTP POST /resource
* *DELETE* - maps to HTTP DELETE /resource/:id

In order to use JsonRestRouter you need to have a Store implementation (yoke extras) bundles a MongoDb store.


## Create a REST resource

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.java}
final Yoke yoke = new Yoke(vertx);

JsonObject persistorCfg = new JsonObject();
persistorCfg.putString("host", "localhost");
persistorCfg.putNumber("port", 27017);
persistorCfg.putString("address", "mongo.persons");
persistorCfg.putString("db_name", "yoke-extras");

final EventBus eb = vertx.eventBus();

// deploy mongo module
container.deployModule("io.vertx~mod-mongo-persistor~2.0.0-beta1", persistorCfg);

// db access
final MongoDbStore db = new MongoDbStore(eb, "mongo.persons");

JsonRestRouter router = new JsonRestRouter(db);
router.rest("/persons", "persons");
yoke.use(router);
yoke.listen(8080);

container.logger().info("Yoke server listening on port 8080");
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

All you need to define is a Database store and after create a router with at least 2 arguments:

* resource - the root resource for the REST call (e.g.: */persons*)
* entityName - the entity name, in mongoDb terms this is the collection name (e.g.: *persons*)
* allowedVerbs - a sum of all verbs as described before, or in case of not specified all are included