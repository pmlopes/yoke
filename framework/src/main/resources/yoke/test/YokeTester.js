function YokeTester(yoke, ssl) {
    this.jYokeTester = new com.jetdrone.vertx.yoke.test.YokeTester(yoke.jYoke, ssl || false);
}

YokeTester.prototype.request = function(method, url, headers, body, callback) {
    // 3 args
    if (body === undefined && callback === undefined) {
        this.jYokeTester.request(method, url, new org.vertx.java.core.Handler({
            handle: headers
        }));
    }
    // 4 args
    else if (callback === undefined) {
        this.jYokeTester.request(method, url, toMultiMap(headers), new org.vertx.java.core.Handler({
            handle: body
        }));
    }
    // 5 args
    else {
        this.jYokeTester.request(method, url, toMultiMap(headers), new org.vertx.java.core.buffer.Buffer(body), new org.vertx.java.core.Handler({
            handle: callback
        }));
    }
};

function toMultiMap(json) {
    var headers = new org.vertx.java.core.http.CaseInsensitiveMultiMap();
    for (var k in json) {
        if (json.hasOwnProperty(k)) {
            headers.add(k, json[k]);
        }
    }
    return headers;
}

module.exports = YokeTester;
