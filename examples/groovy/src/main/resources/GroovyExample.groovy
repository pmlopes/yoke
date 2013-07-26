import com.jetdrone.vertx.yoke.middleware.*
import com.jetdrone.vertx.yoke.GYoke
import com.jetdrone.vertx.yoke.engine.GroovyTemplateEngine

new GYoke(vertx, container.logger)
  .engine('html', new GroovyTemplateEngine())
  .use(new ErrorHandler(true))
  .use(new GRouter()
    .get("/hello") { request ->
        request.response.end "Hello World!"
    }
    .get("/template") { request, next ->
        request.put('session', [id: '1'])
        request.response.render 'template.html', next
    })
  .use {request ->
    request.response.end "maybe you should go to /hello or /template!"
  }.listen(8080)