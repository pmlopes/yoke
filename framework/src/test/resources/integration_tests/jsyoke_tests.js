var vertxTests = require('vertx_tests');
var vassert = require('vertx_assert');

var vertx = require('vertx');

var Yoke = require('yoke/Yoke');

// The test methods must begin with "test"

function testJS() {

    var yoke = new Yoke();
    // all resources are forbidden
    yoke.use(function (req, next) {
        next(401);
    });

    yoke.listen(8888);

    // The server is listening so send an HTTP request
    vertx.createHttpClient().port(8888).getNow("/", function(resp) {
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
    server.listen(8888);

    // The server is listening so send an HTTP request
    vertx.createHttpClient().port(8888).getNow("/", function(resp) {
        vassert.assertTrue(401 == resp.statusCode());
        vassert.testComplete();
    });
}

var Router  = require('yoke/middleware/Router');

function testJSListenToServer() {

    var server = vertx.createHttpServer();
    var yoke = new Yoke();

    var router = new Router();
    yoke.use(router);

    router.get('/api/:userId', function (req, next) {
        vassert.assertTrue(!req.isSecure());
        req.response.end('OK');
    });

    router.param('userId', /[1-9][0-9]/);

    yoke.listen(server);
    server.listen(8888);

    // the pattern expects 2 digits
    vertx.createHttpClient().port(8888).getNow('/api/10', function(resp) {
        vassert.assertTrue(200 == resp.statusCode());
        vassert.testComplete();
    });
}


vertxTests.startTests(this);

