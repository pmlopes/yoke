var Yoke = require('com/jetdrone/vertx/yoke/Yoke');
var Router = require('com/jetdrone/vertx/yoke/middleware/Router');

var yoke = new Yoke();
var router = new Router();

yoke.use(router);
router.get('/hello', function (req) {
    req.response().end('Hi There!');
});
// all other resources are forbidden
yoke.use(function (req, next) {
    next(401);
});
// add the EJS render engine
yoke.engine('ejs', new EJSEngine());

// start server
yoke.listen(8080);