var vertxTests = require('vertx_tests');
var vassert = require('vertx_assert');

// The test methods must begin with "test"

function testJS() {
    vassert.testComplete();
}

vertxTests.startTests(this);

