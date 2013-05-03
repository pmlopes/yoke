var Yoke = require('yokejs/Yoke');
var Router = require('yokejs/middleware/Router');
var EJSEngine = require('yokejs/engine/EJSEngine');

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