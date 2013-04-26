import com.jetdrone.vertx.yoke.middleware.*
import com.jetdrone.vertx.yoke.GYoke
import com.jetdrone.vertx.yoke.engine.GroovyTemplateEngine

def yoke = new GYoke(vertx)

yoke.engine('html', new GroovyTemplateEngine())

yoke.use(new ErrorHandler(true))

yoke.use {request, next ->
    request.put('session', [id: '1'])
    request.response.render 'template.html', next
}

yoke.listen(8080)