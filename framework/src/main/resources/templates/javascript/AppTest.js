var vertxTests = require('vertx_tests');
var vassert = require('vertx_assert');

var vertx = require('vertx');

var Yoke = require('yoke/Yoke');
var YokeTester = require('yoke/test/YokeTester');

// The test methods must begin with "test"

function testGetBase() {

    var yoke = new Yoke();
    // all resources are OK
    yoke.use(function (request) {
        request.response.end('OK');
    });

    new YokeTester(yoke).request('GET', '/', function(resp) {
        vassert.assertTrue(200 == resp.statusCode);
        vassert.testComplete();
    });
}

vertxTests.startTests(this);

