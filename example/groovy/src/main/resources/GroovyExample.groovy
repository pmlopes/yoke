import com.jetdrone.vertx.yoke.middleware.*
import com.jetdrone.vertx.yoke.GYoke
import com.jetdrone.vertx.yoke.engine.GroovyTemplateEngine

def yoke = new GYoke(vertx)

yoke.engine('html', new GroovyTemplateEngine())

yoke.chain(new ErrorHandler(true))
yoke.chain(new GRouter() {{
    get("/hello") { request ->
        request.response.end "Hello World!"
    }
    get("/template") { request ->
        request.put('session', [id: '1'])
        request.response.render 'template.html'
    }
}})

yoke.chain {request ->
    request.response.end "maybe you should go to /hello or /template!"
}

yoke.listen(8080)