var Yoke = require('com/jetdrone/vertx/yoke/Yoke');
var Router = require('com/jetdrone/vertx/yoke/middleware/Router');
var EJSEngine = require('com/jetdrone/vertx/yoke/engine/EJSEngine');

var yoke = new Yoke();
var router = new Router();

yoke.use(router);
router.get('/hello', function (req) {
    req.response().render('/home/plopes/Projects/oss/yoke/example/javascript/src/main/resources/template.ejs');
});
// all other resources are forbidden
yoke.use(function (req, next) {
    next(401);
});

// engines
yoke.engine('ejs', new EJSEngine());

// start server
yoke.listen(8080);