var vertxTests = require('vertx_tests');
var vassert = require('vertx_assert');

var vertx = require('vertx');

var Yoke = require('yokejs/Yoke');

// The test methods must begin with "test"

function testJS() {

    var yoke = new Yoke();
    // all resources are forbidden
    yoke.use(function (req, next) {
        next(401);
    });

    yoke.listen(8080);

    // The server is listening so send an HTTP request
    vertx.createHttpClient().port(8080).getNow("/", function(resp) {
        vassert.assertTrue(401 == resp.statusCode());
        vassert.testComplete();
    });
}

function testJSListenToServer() {

    var server = vertx.createHttpServer();
    var yoke = new Yoke();
    // all resources are forbidden
    yoke.use(function (req, next) {
        next(401);
    });

    yoke.listen(server);
    server.listen(8080);

    // The server is listening so send an HTTP request
    vertx.createHttpClient().port(8080).getNow("/", function(resp) {
        vassert.assertTrue(401 == resp.statusCode());
        vassert.testComplete();
    });
}

vertxTests.startTests(this);

