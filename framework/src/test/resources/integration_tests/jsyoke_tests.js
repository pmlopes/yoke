var vertxTests = require('vertx_tests');
var vassert = require('vertx_assert');
var console = require('vertx/console');

var vertx = require('vertx');

var Yoke = require('yoke/Yoke');
var YokeTester = require('yoke/test/YokeTester');

// The test methods must begin with "test"

function testJSListenToServer1() {

    var yoke = new Yoke();
    // all resources are forbidden
    yoke.use(function (req, next) {
        next(401);
    });

    new YokeTester(yoke).request('GET', '/', {'x-powered-by': 'yoke/1.1'}, function(resp) {
        vassert.assertTrue(401 == resp.statusCode);
        vassert.testComplete();
    });
}

var Router  = require('yoke/middleware/Router');

function testJSListenToServer2() {

    var yoke = new Yoke();

    var router = new Router();
    yoke.use(router);

    router.get('/api/:userId', function (req) {
        vassert.assertTrue(!req.secure);
        req.response.end('OK');
    });

    router.param('userId', /[1-9][0-9]/);

    new YokeTester(yoke).request('GET', '/api/10', function(resp) {
        vassert.assertTrue(200 == resp.statusCode);
        vassert.testComplete();
    });
}

function testIterateParams() {

    var yoke = new Yoke();
    // all resources are forbidden
    yoke.use(function (req, next) {
        for (var p in req.params) {
            vassert.assertEquals(p, 'p' + req.params[p]);
//            console.log(p + ': ' + req.params[p]);
        }
        next(401);
    });

    new YokeTester(yoke).request('GET', '/?p1=1&p2=2&p3=3', function(resp) {
        vassert.assertTrue(401 == resp.statusCode);
        vassert.testComplete();
    });
}

function testResponseCode() {

    var yoke = new Yoke();
    yoke.use(function (req) {
        req.response.statusCode = 503;
        req.response.statusMessage = 'Blah!';
        req.response.end();

    });

    new YokeTester(yoke).request('GET', '/', function(resp) {
        vassert.assertTrue(503 == resp.statusCode);
        vassert.assertEquals('Blah!', resp.statusMessage);
        vassert.testComplete();
    });
}

function testHandlers() {
    var yoke = new Yoke();
    yoke.use(function (req) {
        req.bodyHandler(function(buffer) {
            vassert.assertEquals('HELLO', buffer);
        });
        req.response.end();

    });

    new YokeTester(yoke).request('GET', '/', {}, 'HELLO', function() {
        vassert.testComplete();
    });
}

vertxTests.startTests(this);

