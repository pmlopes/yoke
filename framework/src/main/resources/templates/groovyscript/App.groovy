package templates.groovyscript

import com.jetdrone.vertx.yoke.middleware.*
import com.jetdrone.vertx.yoke.GYoke
import com.jetdrone.vertx.yoke.engine.GroovyTemplateEngine



// Create a new Yoke Application
GYoke app = new GYoke(vertx, container)
// define engines
app.engine(new GroovyTemplateEngine("views"))
// define middleware
app.use(new Favicon())
app.use(new Logger())
app.use(new BodyParser())
app.use(new MethodOverride())
// Create a new Router
GRouter router = new GRouter()
app.use(router)
// static file server
app.use(new Static('public'))

// development only
if (System.getenv('DEV') != null) {
    app.use(new ErrorHandler(true))
}

// define routes
router.get('/') { request ->
    request.put('title', 'My Yoke Application')
    request.response.render('views/index.gsp')
}

// define routes
router.get('/users') { request ->
    request.response.end('respond with a resource')
}

app.listen(8080)
container.logger.info('Yoke server listening on port 8080')