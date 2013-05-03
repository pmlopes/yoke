var Yoke = require('yokejs/Yoke');
var Router = require('yokejs/middleware/Router');
var MicroTemplateEngine = require('yokejs/engine/MicroTemplateEngine');

var yoke = new Yoke();
var router = new Router();

yoke.use(router);
router.get('/hello', function (req) {
    req.put('name', 'World');
    req.response().render('/home/plopes/Projects/oss/yoke/example/javascript/src/main/resources/template.html');
});
// all other resources are forbidden
yoke.use(function (req, next) {
    next(401);
});

// engines
yoke.engine('html', new MicroTemplateEngine());

// start server
yoke.listen(8080);