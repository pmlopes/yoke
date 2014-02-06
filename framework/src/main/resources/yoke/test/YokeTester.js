function YokeTester(yoke, ssl) {
    this.jYokeTester = new com.jetdrone.vertx.yoke.test.YokeTester(yoke.vertx, yoke.jYoke, ssl || false);
}

YokeTester.prototype.request = function(method, url, headers, body, callback) {
    // TODO: convert headers from js object to multimap
    // TODO: what is a buffer in rhino?
    // 3 args
    if (body === undefined && callback === undefined) {
        this.jYokeTester.request(method, url, new org.vertx.java.core.Handler({
            handle: headers
        }));
    }
    // 4 args
    else if (callback === undefined) {
        this.jYokeTester.request(method, url, headers, new org.vertx.java.core.Handler({
            handle: headers
        }));
    } else {
        this.jYokeTester.request(method, url, headers, body, new org.vertx.java.core.Handler({
            handle: headers
        }));
    }
};

module.exports = YokeTester;
