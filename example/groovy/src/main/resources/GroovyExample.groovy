import com.jetdrone.vertx.yoke.*
import com.jetdrone.vertx.yoke.middleware.*

def yoke = new GYoke(vertx)

yoke.use(new ErrorHandler(true))

yoke.use() { request, next ->
    if (request.path() == '/hello') {
        request.response().end('Hi there!')
    } else {
        next.handle('You did not say hello! /hello')
    }
}

yoke.listen(8080)
