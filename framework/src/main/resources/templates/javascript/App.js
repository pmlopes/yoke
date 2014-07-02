var console = require('vertx/console');

var Yoke = require('yoke/Yoke');
var MicroTemplateEngine = require('yoke/engine/MicroTemplateEngine');
var Favicon = require('yoke/middleware/Favicon');
var Logger = require('yoke/middleware/Logger');
var BodyParser = require('yoke/middleware/BodyParser');
var MethodOverride = require('yoke/middleware/MethodOverride');
var Router = require('yoke/middleware/Router');
var Static = require('yoke/middleware/Static');
var ErrorHandler = require('yoke/middleware/ErrorHandler');

// Create a new Yoke Application
var app = new Yoke();
// define engines
app.engine(new MicroTemplateEngine());
// define middleware
app.use(new Favicon());
app.use(new Logger());
app.use(new BodyParser());
app.use(new MethodOverride());
// Create a new Router
var router = new Router();
app.use(router);
// static file server
app.use(new Static('public'));

app.use(new ErrorHandler(true));

// define routes
router.get('/', function(request) {
  request['title'] = 'My Yoke Application';
  request.response.render('views/index.ejs');
});

// define routes
router.get('/users', function (request) {
    request.response.end('respond with a resource');
});

// start server
app.listen(8080);
console.log("Yoke server listening on port 8080");